package io.github.anjoismysign.skeramidcommands.server;

import io.github.anjoismysign.skeramidcommands.command.Command;
import org.jetbrains.annotations.NotNull;

public interface ServerAdapter<T> {
    /**
     * Gets the underlying object of the adapter.
     *
     * @param permissionMessenger the PermissionMessenger.
     * @return the underlying object.
     */
    @NotNull
    T of(PermissionMessenger permissionMessenger);

    /**
     * Adapts an object to the PermissionMessenger.
     *
     * @param object the object.
     * @return the PermissionMessenger.
     */
    @NotNull
    PermissionMessenger adapt(Object object);

    /**
     * Gets the server API.
     *
     * @return the server API.
     */
    @NotNull
    ServerAPI getServerAPI();

    /**
     * Creates a command.
     *
     * @param name        the name of the command.
     * @param permission  the permission of the command.
     * @param description the description of the command.
     * @return the command.
     */
    @NotNull
    Command createCommand(@NotNull String name, @NotNull String permission, @NotNull String description);
}
