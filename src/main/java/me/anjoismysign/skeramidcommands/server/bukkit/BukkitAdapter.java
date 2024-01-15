package me.anjoismysign.skeramidcommands.server.bukkit;

import me.anjoismysign.skeramidcommands.command.Command;
import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import me.anjoismysign.skeramidcommands.server.ServerAPI;
import me.anjoismysign.skeramidcommands.server.ServerAdapter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BukkitAdapter implements ServerAdapter<CommandSender> {
    private static BukkitAdapter instance;

    public static BukkitAdapter getInstance() {
        if (instance == null) {
            instance = new BukkitAdapter();
        }
        return instance;
    }

    @NotNull
    public CommandSender of(PermissionMessenger permissionMessenger) {
        if (permissionMessenger instanceof CommandSenderAdapter) {
            return ((CommandSenderAdapter) permissionMessenger).getCommandSender();
        }
        throw new IllegalArgumentException("'permissionMessenger' is not a BukkitAdapter");
    }

    @NotNull
    public CommandSenderAdapter adapt(Object object) {
        if (object instanceof CommandSender) {
            return new CommandSenderAdapter((CommandSender) object);
        }
        throw new IllegalArgumentException("Sender is not a Bukkit CommandSender");
    }

    @NotNull
    public ServerAPI getServerAPI() {
        return ServerAPI.BUKKIT;
    }

    @NotNull
    public Command createCommand(@NotNull String name, @NotNull String permission, @NotNull String description) {
        return BukkitPyramid.of(name, permission, description).getPyramid();
    }

    /**
     * Creates a new command through a BukkitCommand, using the plugin.yml file.
     *
     * @param name the name of the command.
     * @return the command.
     */
    @NotNull
    public Command ofBukkitCommand(@NotNull String name) {
        return BukkitPyramid.ofBukkitCommand(name).getPyramid();
    }


}
