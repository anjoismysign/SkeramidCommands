package me.anjoismysign.skeramidcommands;

import me.anjoismysign.skeramidcommands.command.Command;
import me.anjoismysign.skeramidcommands.command.CommandBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class SkeramidCommands extends JavaPlugin {

    @Override
    public void onEnable() {
        Command command = CommandBuilder
                .of("factions")
                .permission("factions")
                .build();
        Command points = command.child("points");
        points.onExecute(messenger -> messenger.sendMessage("Factions points"));
        points.child("info").onExecute(messenger -> messenger.sendMessage("Factions points info"));
    }
}