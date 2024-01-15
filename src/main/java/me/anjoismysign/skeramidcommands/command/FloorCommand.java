package me.anjoismysign.skeramidcommands.command;

import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FloorCommand implements Command {
    private final List<String> args;
    private final PyramidCommand pyramid;
    private final String name, permission;
    private final List<String> alias;
    @Nullable
    private Consumer<PermissionMessenger> onExecute;

    protected FloorCommand(List<String> args, PyramidCommand pyramid) {
        this(args, pyramid, new ArrayList<>());
    }

    protected FloorCommand(List<String> args, PyramidCommand pyramid,
                           List<String> alias) {
        this.args = args;
        this.pyramid = pyramid;
        this.alias = alias;
        this.name = String.join(" ", args).toLowerCase();
        String parentPermission = pyramid.getPermission();
        this.permission = parentPermission.isEmpty() ? "" : (pyramid.getPermission() + "." + String
                .join(".", args)).toLowerCase();
    }

    @NotNull
    public Command child(String name) {
        List<String> dupe = new ArrayList<>(args);
        dupe.add(name);
        FloorCommand command = new FloorCommand(dupe, pyramid);
        pyramid.getChildren().add(command);
        return command;
    }

    @Override
    public void onExecute(Consumer<PermissionMessenger> consumer) {
        this.onExecute = consumer;
    }

    @Override
    public boolean run(PermissionMessenger permissionMessenger) {
        if (!isAuthorized(permissionMessenger))
            return false;
        if (onExecute != null)
            onExecute.accept(permissionMessenger);
        return true;
    }

    public boolean isAuthorized(PermissionMessenger permissionMessenger) {
        return permissionMessenger.hasPermission(permission);
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
        return pyramid.getDescription();
    }

    @NotNull
    public Command getParent() {
        return Objects.requireNonNull(pyramid.findChildren(name), "Parent not found");
    }


    @NotNull
    public String toString() {
        return "{pyramid=" + this.pyramid.getName() +
                ", name=" + this.name +
                ", alias=" + this.alias +
                ", permission=" + this.permission +
                ", description=" + this.pyramid.getDescription() +
                "}";
    }
}
