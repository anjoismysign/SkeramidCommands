package me.anjoismysign.skeramidcommands.command;

import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import me.anjoismysign.skeramidcommands.throwable.ChildNotAllowedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PyramidCommand implements Command {

    @NotNull
    private final String name, permission, description;
    @NotNull
    private final List<FloorCommand> children;
    @NotNull
    private final List<CommandTarget<?>> parameters;
    @NotNull
    private final List<String> usage;
    @Nullable
    private BiConsumer<PermissionMessenger, String[]> onExecute;

    public PyramidCommand(@NotNull String name,
                          @NotNull String permission,
                          @NotNull String description) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.parameters = new ArrayList<>();
        this.children = new ArrayList<>();
        this.usage = new ArrayList<>();
    }

    @Nullable
    public final List<String> tabComplete(PermissionMessenger permissionMessenger, List<String> args) {
        if (args.isEmpty())
            return null;
        int size = args.size();
        List<String> dupe = new ArrayList<>(args);
        dupe.remove(dupe.size() - 1);
        String prefix = String.join(" ", dupe).toLowerCase(Locale.ROOT);
        List<Command> children = getChildren()
                .stream()
                .filter(child -> child.getName().startsWith(prefix))
                .filter(child -> child.getName().split(" ").length == size)
                .filter(child -> child.isAuthorized(permissionMessenger))
                .collect(Collectors.toList());
        List<String> single = children
                .stream()
                .map(child -> child.getName().split(" ")[size - 1])
                .collect(Collectors.toList());
        if (!single.isEmpty())
            return single;
        Command find = findParameterizedChildren(prefix);
        if (find == null) {
            int argumentSize;
            if (prefix.trim().isEmpty()) {
                argumentSize = 1;
            } else {
                String commandName = this.getName();
                String[] subArgs = prefix.replace(commandName, "").split(" ");
                argumentSize = subArgs.length + 1;
            }
            if (this.getParameters().size() < argumentSize)
                return new ArrayList<>();
            CommandTarget<?> result = this.getParameters().get(argumentSize - 1);
            if (result != null)
                return result.get();
        }
        String commandName = find.getName();
        String[] subArgs = prefix.replace(commandName, "").split(" ");
        int argumentSize = subArgs.length;
        if (find.getParameters().size() < argumentSize)
            return new ArrayList<>();
        CommandTarget<?> result = find.getParameters().get(argumentSize - 1);
        if (result != null)
            return result.get();
        return new ArrayList<>();
    }

    @Nullable
    private Command findParameterizedChildren(String input) {
        String[] split = input.split(" ");
        Command children = getChildren()
                .stream()
                .filter(child -> child.getName().equals(input))
                .filter(Command::hasParameters)
                .findFirst().orElse(null);
        if (children == null) {
            if (split.length == 1)
                return null;
            String[] newArray = Arrays.copyOf(split, split.length - 1);
            String newString = String.join(" ", newArray);
            return findParameterizedChildren(newString);
        }
        return children;
    }

    public final boolean execute(PermissionMessenger permissionMessenger, List<String> args) {
        if (args.isEmpty()) {
            sendUsage(permissionMessenger);
            if (hasParameters())
                return true;
            run(permissionMessenger);
            return true;
        }
        String prefix = String.join(" ", args).toLowerCase(Locale.ROOT);
        Command find = findChildren(prefix);
        if (find == null) {
            if (getParameters().isEmpty())
                return false;
            String commandName = this.getName();
            String[] subArgs = prefix.replace(commandName, "").trim().split(" ");
            run(permissionMessenger, subArgs);
            return true;
        }
        if (find.hasParameters()) {
            String commandName = find.getName();
            String[] subArgs = prefix.replace(commandName, "").trim().split(" ");
            return find.run(permissionMessenger, subArgs);
        }
        return find.run(permissionMessenger);
    }

    public final void sendUsage(PermissionMessenger permissionMessenger) {
        String command = getName();
        permissionMessenger.sendMessage(String.format("%s | Help", command));
        getUsage().forEach(permissionMessenger::sendMessage);
        List<FloorCommand> children = getChildren();
        if (children.isEmpty())
            return;
        permissionMessenger.sendMessage("");
        for (Command argument : getChildren()) {
            permissionMessenger.sendMessage(String.format("/%s %s - %s", command, argument.getName(), argument.getDescription()));
        }
    }

    public void onExecute(BiConsumer<PermissionMessenger, String[]> consumer) {
        this.onExecute = consumer;
    }

    public @NotNull List<CommandTarget<?>> getParameters() {
        return parameters;
    }

    public void setParameters(CommandTarget<?>... targets) {
        parameters.clear();
        Collections.addAll(parameters, targets);
    }

    @NotNull
    public List<String> getUsage() {
        return Collections.unmodifiableList(usage);
    }

    public void addUsage(@NotNull String... usage) {
        this.usage.addAll(Arrays.asList(usage));
    }

    public boolean run(PermissionMessenger permissionMessenger, String... args) {
        if (!isAuthorized(permissionMessenger))
            return false;
        if (onExecute != null)
            onExecute.accept(permissionMessenger, args);
        return true;
    }

    public final boolean isAuthorized(PermissionMessenger permissionMessenger) {
        return permission.isEmpty() || permissionMessenger.hasPermission(this.permission);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getPermission() {
        return permission;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * Finds a child by either name or alias.
     *
     * @param name the name or alias.
     * @return the child. Null if not found.
     */
    @Nullable
    public Command findChildren(String name) {
        Command single = children.stream().filter(child -> child.getName().equals(name))
                .findFirst().orElse(null);
        if (single != null)
            return single;
        return findParameterizedChildren(name);
    }

    @NotNull
    public List<FloorCommand> getChildren() {
        return children;
    }

    @NotNull
    public String toString() {
        return "{name=" + this.name +
                ", permission=" + this.permission +
                ", description=" + this.description +
                "}";
    }

    @NotNull
    public Command getParent() {
        return this;
    }

    @NotNull
    public Command child(String name) {
        List<String> args = new ArrayList<>();
        args.add(name);
        Command children = findChildren(name);
        if (children == null) {
            if (hasParameters())
                throw ChildNotAllowedException.of(this, name);
            FloorCommand floorCommand = new FloorCommand(args, this);
            this.children.add(floorCommand);
            return floorCommand;
        }
        return children;
    }
}
