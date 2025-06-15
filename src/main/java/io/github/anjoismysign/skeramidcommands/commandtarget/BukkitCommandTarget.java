package io.github.anjoismysign.skeramidcommands.commandtarget;

import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BukkitCommandTarget {

    private static final CommandTarget<Player> ONLINE_PLAYERS;
    private static final CommandTarget<World> WORLD_NAMES;

    static {
        ONLINE_PLAYERS = new CommandTarget<Player>() {
            @Override
            public List<String> get() {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .collect(Collectors.toList());
            }

            @Override
            public Player parse(String arg) {
                if (arg.trim().isEmpty())
                    return null;
                return Bukkit.getPlayer(arg);
            }
        };

        WORLD_NAMES = new CommandTarget<World>() {
            @Override
            public List<String> get() {
                return Bukkit.getWorlds().stream().map(World::getName)
                        .collect(Collectors.toList());
            }

            @Nullable
            @Override
            public World parse(String arg) {
                if (arg.trim().isEmpty())
                    return null;
                return Bukkit.getWorld(arg);
            }
        };

    }

    public static CommandTarget<Player> ONLINE_PLAYERS() {
        return ONLINE_PLAYERS;
    }

    public static CommandTarget<World> WORLD_NAMES() {
        return WORLD_NAMES;
    }
}
