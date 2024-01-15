package me.anjoismysign.skeramidcommands;

import me.anjoismysign.skeramidcommands.command.Command;
import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import me.anjoismysign.skeramidcommands.server.ServerAPI;
import me.anjoismysign.skeramidcommands.server.ServerAdapter;
import me.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import me.anjoismysign.skeramidcommands.server.sponge.SpongeAdapter;
import org.jetbrains.annotations.NotNull;

public class SkeramidCommandsAPI {
    private static SkeramidCommandsAPI instance;
    private final ServerAdapter<?> serverAdapter;

    private SkeramidCommandsAPI(ServerAdapter<?> serverAdapter) {
        this.serverAdapter = serverAdapter;
    }

    public static SkeramidCommandsAPI getInstance() {
        if (instance == null) {
            ServerAdapter<?> serverAdapter = null;
            boolean bukkit = false;
            boolean sponge = false;
            try {
                Class.forName(ServerAPI.BUKKIT.getImplementationPath());
                bukkit = true;
            } catch (ClassNotFoundException ignored) {
            }
            try {
                Class.forName(ServerAPI.SPONGE.getImplementationPath());
                sponge = true;
            } catch (ClassNotFoundException ignored) {
            }
            if (bukkit) {
                serverAdapter = BukkitAdapter.getInstance();
            } else if (sponge) {
                serverAdapter = SpongeAdapter.getInstance();
            }
            instance = new SkeramidCommandsAPI(serverAdapter);
        }
        return instance;
    }

    /**
     * Checks whether the server has an adapter.
     *
     * @return true if the server has an adapter, false otherwise.
     */
    public boolean hasAdapter() {
        return serverAdapter != null;
    }

    /**
     * Will adapt an object to the PermissionMessenger.
     *
     * @param object the permission adaptable.
     * @return the PermissionMessenger.
     */
    @NotNull
    public PermissionMessenger adapt(Object object) {
        if (!hasAdapter())
            throw new IllegalStateException("No adapter found for current server software!");
        return serverAdapter.adapt(object);
    }

    /**
     * Creates a command.
     *
     * @param name        the name of the command.
     * @param permission  the permission of the command.
     * @param description the description of the command.
     * @return the command.
     */
    @NotNull
    public Command createCommand(@NotNull String name,
                                 @NotNull String permission,
                                 @NotNull String description) {
        if (!hasAdapter())
            throw new IllegalStateException("No adapter found for current server software!");
        return serverAdapter.createCommand(name, permission, description);
    }
}
