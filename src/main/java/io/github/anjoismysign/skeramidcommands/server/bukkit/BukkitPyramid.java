package io.github.anjoismysign.skeramidcommands.server.bukkit;

import io.github.anjoismysign.skeramidcommands.command.PyramidCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BukkitPyramid implements CommandExecutor, TabCompleter {
    private final PyramidCommand pyramid;

    private BukkitPyramid(PyramidCommand command) {
        this.pyramid = command;
    }

    @NotNull
    public static BukkitPyramid ofBukkitCommand(@NotNull String name) {
        PluginCommand pluginCommand = Objects.requireNonNull(Bukkit.getPluginCommand(name),
                "Command '" + name + "' is not registered in plugin.yml");
        String permission = pluginCommand.getPermission() == null ? "" : pluginCommand.getPermission();
        return of(name, permission, pluginCommand.getDescription());
    }

    @NotNull
    public static BukkitPyramid of(@NotNull String name,
                                   @NotNull String permission,
                                   @NotNull String description) {
        PyramidCommand command = new PyramidCommand(name, permission, description);
        BukkitPyramid pyramid = new BukkitPyramid(command);
        String commandName = command.getName();
        PluginCommand pluginCommand = Objects.requireNonNull(Bukkit.getPluginCommand(commandName),
                "Command '" + commandName + "' is not registered in plugin.yml");
        pluginCommand.setExecutor(pyramid);
        pluginCommand.setTabCompleter(pyramid);
        return pyramid;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return pyramid.execute(BukkitAdapter.getInstance().adapt(commandSender), new ArrayList<>(Arrays.asList(args)));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return pyramid.tabComplete(BukkitAdapter.getInstance().adapt(commandSender), new ArrayList<>(Arrays.asList(args)));
    }

    @NotNull
    public PyramidCommand getPyramid() {
        return pyramid;
    }
}
