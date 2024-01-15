package me.anjoismysign.skeramidcommands.command;

import me.anjoismysign.skeramidcommands.SkeramidCommandsAPI;
import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CommandBuilder {
    @NotNull
    private String name, permission, description;
    @Nullable
    private List<String> alias;
    @Nullable
    private Consumer<PermissionMessenger> onExecute;

    private CommandBuilder(@NotNull String name) {
        this.name = name;
        this.permission = "";
        this.description = "";
    }

    public static CommandBuilder of(@NotNull String name) {
        Objects.requireNonNull(name, "'name' cannot be null");
        return new CommandBuilder(name);
    }

    public CommandBuilder name(@NotNull String name) {
        Objects.requireNonNull(name, "'name' cannot be null");
        this.name = name;
        return this;
    }

    public CommandBuilder permission(@NotNull String permission) {
        Objects.requireNonNull(permission, "'permission' cannot be null");
        this.permission = permission;
        return this;
    }

    public CommandBuilder description(@NotNull String description) {
        Objects.requireNonNull(description, "'description' cannot be null");
        this.description = description;
        return this;
    }

    public CommandBuilder onExecute(@Nullable Consumer<PermissionMessenger> onExecute) {
        this.onExecute = onExecute;
        return this;
    }

    public CommandBuilder alias(@Nullable List<String> alias) {
        this.alias = alias;
        return this;
    }

    public CommandBuilder alias(String... alias) {
        this.alias = Arrays.asList(alias);
        return this;
    }

    @NotNull
    public Command build() {
        Command command = SkeramidCommandsAPI.getInstance().createCommand(name, permission, description);
        if (alias != null)
            command.getAlias().addAll(alias);
        if (onExecute != null)
            command.onExecute(onExecute);
        return command;
    }
}
