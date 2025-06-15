package io.github.anjoismysign.skeramidcommands.command;

import io.github.anjoismysign.skeramidcommands.server.PermissionMessenger;
import io.github.anjoismysign.skeramidcommands.throwable.ChildNotAllowedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;

public class FloorCommand implements Command {
    @NotNull
    private final List<String> args;
    @NotNull
    private final PyramidCommand pyramid;
    @NotNull
    private final String name, permission;
    @NotNull
    private final List<CommandTarget<?>> parameters;
    @Nullable
    private BiConsumer<PermissionMessenger, String[]> onExecute;

    protected FloorCommand(@NotNull List<String> args, PyramidCommand pyramid) {
        this.args = args;
        this.pyramid = pyramid;
        this.parameters = new ArrayList<>();
        this.name = String.join(" ", args).toLowerCase(Locale.ROOT);
        String parentPermission = pyramid.getPermission();
        this.permission = parentPermission.isEmpty() ? "" : (pyramid.getPermission() + "." + String
                .join(".", args)).toLowerCase(Locale.ROOT);
    }

    @NotNull
    public Command child(String name) {
        if (hasParameters())
            throw ChildNotAllowedException.of(this, name);
        List<String> dupe = new ArrayList<>(args);
        dupe.add(name);
        FloorCommand command = new FloorCommand(dupe, pyramid);
        pyramid.getChildren().add(command);
        return command;
    }

    public @NotNull List<CommandTarget<?>> getParameters() {
        return parameters;
    }

    public void setParameters(CommandTarget<?>... targets) {
        parameters.clear();
        Collections.addAll(parameters, targets);
    }

    public @NotNull List<String> getUsage() {
        return pyramid.getUsage();
    }

    public void addUsage(@NotNull String... usage) {
        pyramid.addUsage();
    }

    @Override
    public void onExecute(BiConsumer<PermissionMessenger, String[]> consumer) {
        this.onExecute = consumer;

    }

    @Override
    public boolean run(PermissionMessenger permissionMessenger, String... args) {
        if (!isAuthorized(permissionMessenger))
            return false;
        if (onExecute != null)
            onExecute.accept(permissionMessenger, args);
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
                ", permission=" + this.permission +
                ", description=" + this.pyramid.getDescription() +
                "}";
    }
}
