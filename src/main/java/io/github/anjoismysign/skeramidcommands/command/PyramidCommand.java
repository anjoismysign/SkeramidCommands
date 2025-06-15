package io.github.anjoismysign.skeramidcommands.command;

import io.github.anjoismysign.skeramidcommands.server.PermissionMessenger;
import io.github.anjoismysign.skeramidcommands.throwable.ChildNotAllowedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PyramidCommand implements Command {
    @NotNull
    private final String name;
    @NotNull
    private final String permission;
    @NotNull
    private final String description;
    @NotNull
    private final List<FloorCommand> children;
    @NotNull
    private final List<CommandTarget<?>> parameters;
    @NotNull
    private final List<String> usage;
    @Nullable
    private BiConsumer<PermissionMessenger, String[]> onExecute;

    public PyramidCommand(@NotNull String name, @NotNull String permission, @NotNull String description) {
        this.name = name;
        this.permission = permission;
        this.description = description;
        parameters = new ArrayList<>();
        children = new ArrayList<>();
        usage = new ArrayList<>();
    }

    @Nullable
    public final List<String> tabComplete(PermissionMessenger permissionMessenger, List<String> args) {
        if (args.isEmpty()) {
            return null;
        } else {
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
            List<String> single = children.stream().map(child -> child.getName().split(" ")[size - 1]).collect(Collectors.toList());
            if (!single.isEmpty()) {
                return single;
            } else {
                Command find = findParameterizedChildren(prefix);
                if (find == null) {
                    int argumentSize;
                    if (prefix.trim().isEmpty()) {
                        argumentSize = 1;
                    } else {
                        String commandName = getName();
                        String[] subArgs = prefix.replace(commandName, "").split(" ");
                        argumentSize = subArgs.length + 1;
                    }

                    if (getParameters().size() < argumentSize) {
                        return new ArrayList<>();
                    }

                    CommandTarget<?> result = getParameters().get(argumentSize - 1);
                    if (result != null) {
                        return result.get();
                    }
                }

                String commandName = find.getName();
                String[] subArgs = prefix.replace(commandName, "").split(" ");
                int argumentSizex = subArgs.length;
                if (find.getParameters().size() < argumentSizex) {
                    return new ArrayList<>();
                } else {
                    CommandTarget<?> result = find.getParameters().get(argumentSizex - 1);
                    return (List<String>)(result != null ? result.get() : new ArrayList<>());
                }
            }
        }
    }

    @Nullable
    private Command findParameterizedChildren(String input) {
        String[] split = input.split(" ");
        Command children = getChildren().stream().filter(child -> child.getName().equals(input)).filter(Command::hasParameters).findFirst().orElse(null);
        if (children == null) {
            if (split.length == 1) {
                return null;
            } else {
                String[] newArray = Arrays.copyOf(split, split.length - 1);
                String newString = String.join(" ", newArray);
                return findParameterizedChildren(newString);
            }
        } else {
            return children;
        }
    }

    public final boolean execute(PermissionMessenger permissionMessenger, List<String> args) {
        if (args.isEmpty()) {
            sendUsage(permissionMessenger);
            if (!hasParameters()) {
                run(permissionMessenger);
            }
            return true;
        } else {
            String suffix = String.join(" ", args);
            Command find = findChildren(suffix);
            if (find == null) {
                if (getParameters().isEmpty()) {
                    return false;
                } else {
                    String commandName = getName();
                    String[] subArgs = suffix.replace(commandName, "").trim().split(" ");
                    run(permissionMessenger, subArgs);
                    return true;
                }
            } else if (find.hasParameters()) {
                String commandName = find.getName();
                String[] subArgs = suffix.replace(commandName, "").trim().split(" ");
                return find.run(permissionMessenger, subArgs);
            } else {
                return find.run(permissionMessenger);
            }
        }
    }

    public final void sendUsage(PermissionMessenger permissionMessenger) {
        String command = getName();
        permissionMessenger.sendMessage(String.format("%s | Help", command));
        getUsage().forEach(permissionMessenger::sendMessage);
        List<FloorCommand> children = getChildren();
        if (!children.isEmpty()) {
            permissionMessenger.sendMessage("");

            for (Command argument : getChildren()) {
                permissionMessenger.sendMessage(String.format("/%s %s - %s", command, argument.getName(), argument.getDescription()));
            }
        }
    }

    @Override
    public void onExecute(BiConsumer<PermissionMessenger, String[]> consumer) {
        onExecute = consumer;
    }

    @NotNull
    @Override
    public List<CommandTarget<?>> getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(CommandTarget<?>... targets) {
        parameters.clear();
        Collections.addAll(parameters, targets);
    }

    @NotNull
    @Override
    public List<String> getUsage() {
        return Collections.unmodifiableList(usage);
    }

    @Override
    public void addUsage(@NotNull String... usage) {
        this.usage.addAll(Arrays.asList(usage));
    }

    @Override
    public boolean run(PermissionMessenger permissionMessenger, String... args) {
        if (!isAuthorized(permissionMessenger)) {
            return false;
        } else {
            if (onExecute != null) {
                onExecute.accept(permissionMessenger, args);
            }

            return true;
        }
    }

    @Override
    public final boolean isAuthorized(PermissionMessenger permissionMessenger) {
        return permission.isEmpty() || permissionMessenger.hasPermission(permission);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getPermission() {
        return permission;
    }

    @NotNull
    @Override
    public String getDescription() {
        return description;
    }

    @Nullable
    public Command findChildren(String name) {
        String lowerCased = name.toLowerCase(Locale.ROOT);
        Command single = children.stream().filter(child -> child.getName().equals(lowerCased)).findFirst().orElse(null);
        return single != null ? single : findParameterizedChildren(lowerCased);
    }

    @NotNull
    public List<FloorCommand> getChildren() {
        return children;
    }

    @NotNull
    @Override
    public String toString() {
        return "{name=" + name + ", permission=" + permission + ", description=" + description + "}";
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
            if (hasParameters()) {
                throw ChildNotAllowedException.of(this, name);
            } else {
                FloorCommand floorCommand = new FloorCommand(args, this);
                this.children.add(floorCommand);
                return floorCommand;
            }
        } else {
            return children;
        }
    }
}
