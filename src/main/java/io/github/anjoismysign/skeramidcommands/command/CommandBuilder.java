package io.github.anjoismysign.skeramidcommands.command;

import io.github.anjoismysign.skeramidcommands.SkeramidCommandsAPI;
import io.github.anjoismysign.skeramidcommands.server.PermissionMessenger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

public class CommandBuilder {
    @NotNull
    private String name, permission, description;
    @Nullable
    private BiConsumer<PermissionMessenger, String[]> onExecute;

    private CommandBuilder(@NotNull String name) {
        this.name = name;
        this.permission = name;
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

    public CommandBuilder onExecute(@Nullable BiConsumer<PermissionMessenger, String[]> onExecute) {
        this.onExecute = onExecute;
        return this;
    }

    @NotNull
    public Command build() {
        Command command = SkeramidCommandsAPI.getInstance().createCommand(name, permission, description);
        if (onExecute != null)
            command.onExecute(onExecute);
        return command;
    }
}
