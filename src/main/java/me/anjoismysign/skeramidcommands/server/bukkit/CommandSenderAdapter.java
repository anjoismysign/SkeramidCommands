package me.anjoismysign.skeramidcommands.server.bukkit;

import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import org.bukkit.command.CommandSender;

public class CommandSenderAdapter implements PermissionMessenger {
    private final CommandSender commandSender;

    protected CommandSenderAdapter(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    public boolean hasPermission(String permission) {
        return commandSender.hasPermission(permission);
    }

    public CommandSender getCommandSender() {
        return commandSender;
    }

    public void sendMessage(String message) {
        commandSender.sendMessage(message);
    }
}
