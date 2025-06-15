package io.github.anjoismysign.skeramidcommands.server;

public interface PermissionMessenger {

    /**
     * Checks if this messenger has the given permission.
     *
     * @param permission the permission.
     * @return true if it has the permission, false otherwise.
     */
    boolean hasPermission(String permission);

    /**
     * Sends a message to this messenger.
     *
     * @param message the message.
     */
    void sendMessage(String message);
}
