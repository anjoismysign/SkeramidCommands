package me.anjoismysign.skeramidcommands.command;

import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PyramidCommand implements Command {

    @NotNull
    private final String name, permission, description;
    @NotNull
    private final List<String> alias;
    @NotNull
    private final List<FloorCommand> children;
    @Nullable
    private Consumer<PermissionMessenger> onExecute;

    public PyramidCommand(@NotNull String name,
                          @NotNull String permission,
                          @NotNull String description,
                          @NotNull List<String> alias) {
        this.name = name;
        this.alias = alias;
        this.permission = permission;
        this.description = description;
        this.children = new ArrayList<>();
    }

    public PyramidCommand(@NotNull String name,
                          @NotNull String permission,
                          @NotNull String description) {
        this(name, permission, description, new ArrayList<>());
    }

    @Nullable
    public final List<String> tabComplete(PermissionMessenger permissionMessenger, List<String> args) {
        if (args.isEmpty())
            return null;
        int length = args.size();
        List<String> dupe = new ArrayList<>(args);
        dupe.remove(dupe.size() - 1);
        String prefix = String.join(" ", dupe).toLowerCase();
        List<Command> children = getChildren()
                .stream()
                .filter(child -> child.getName().startsWith(prefix))
                .filter(child -> child.getName().split(" ").length == length)
                .filter(child -> child.isAuthorized(permissionMessenger))
                .collect(Collectors.toList());
        if (children.isEmpty())
            return null;
        return children
                .stream()
                .map(child -> child.getName().split(" ")[length - 1])
                .collect(Collectors.toList());
    }

    public final boolean execute(PermissionMessenger permissionMessenger, List<String> args) {
        if (args.isEmpty()) {
            sendUsage(permissionMessenger);
            run(permissionMessenger);
            return true;
        }
        Command argument = findChildren(String.join(" ", args).toLowerCase());
        if (argument == null)
            return false;
        args.remove(0);
        return argument.run(permissionMessenger);
    }

    public final void sendUsage(PermissionMessenger permissionMessenger) {
        String command = getName();
        permissionMessenger.sendMessage(String.format("%s | Help", command));
        permissionMessenger.sendMessage("");
        for (Command argument : getChildren()) {
            permissionMessenger.sendMessage(String.format("/%s %s - %s", command, argument.getName(), argument.getDescription()));
        }
    }

    public void onExecute(Consumer<PermissionMessenger> consumer) {
        this.onExecute = consumer;
    }

    public boolean run(PermissionMessenger permissionMessenger) {
        if (!isAuthorized(permissionMessenger))
            return false;
        if (onExecute != null)
            onExecute.accept(permissionMessenger);
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
    public List<String> getAlias() {
        return alias;
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
        return children.stream().filter(child -> child.getName().equals(name) ||
                child.getAlias().contains(name)).findFirst().orElse(null);
    }

    @NotNull
    public List<FloorCommand> getChildren() {
        return children;
    }

    @NotNull
    public String toString() {
        return "{name=" + this.name +
                ", alias=" + this.alias +
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
            FloorCommand floorCommand = new FloorCommand(args, this);
            this.children.add(floorCommand);
            return floorCommand;
        }
        return children;
    }
}
