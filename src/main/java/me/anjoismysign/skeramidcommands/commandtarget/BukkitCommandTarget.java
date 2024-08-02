package me.anjoismysign.skeramidcommands.commandtarget;

import me.anjoismysign.skeramidcommands.command.CommandTarget;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BukkitCommandTarget {

    private static final CommandTarget<Player> ONLINE_PLAYERS;
    private static final CommandTarget<EntityType> ENTITY_TYPES;
    private static final CommandTarget<World> WORLD_NAMES;
    private static final CommandTarget<Particle> PARTICLES;
    private static final CommandTarget<ChatColor> CHAT_COLORS;
    private static final CommandTarget<GameMode> GAME_MODES;
    private static final CommandTarget<Instrument> INSTRUMENTS;
    private static final CommandTarget<Material> MATERIALS;
    private static final CommandTarget<Sound> SOUNDS;
    private static final CommandTarget<Statistic> STATISTICS;
    private static final CommandTarget<TreeType> TREE_TYPES;

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

        ENTITY_TYPES = CommandTargetBuilder.fromEnum(EntityType.class);

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

        PARTICLES = CommandTargetBuilder.fromEnum(Particle.class);
        CHAT_COLORS = CommandTargetBuilder.fromEnum(ChatColor.class);
        GAME_MODES = CommandTargetBuilder.fromEnum(GameMode.class);
        INSTRUMENTS = CommandTargetBuilder.fromEnum(Instrument.class);
        MATERIALS = CommandTargetBuilder.fromEnum(Material.class);
        SOUNDS = CommandTargetBuilder.fromEnum(Sound.class);
        STATISTICS = CommandTargetBuilder.fromEnum(Statistic.class);
        TREE_TYPES = CommandTargetBuilder.fromEnum(TreeType.class);
    }

    public static CommandTarget<Player> ONLINE_PLAYERS() {
        return ONLINE_PLAYERS;
    }

    public static CommandTarget<EntityType> ENTITY_TYPES() {
        return ENTITY_TYPES;
    }

    public static CommandTarget<World> WORLD_NAMES() {
        return WORLD_NAMES;
    }

    public static CommandTarget<Particle> PARTICLES() {
        return PARTICLES;
    }

    public static CommandTarget<ChatColor> CHAT_COLORS() {
        return CHAT_COLORS;
    }

    public static CommandTarget<GameMode> GAME_MODES() {
        return GAME_MODES;
    }

    public static CommandTarget<Instrument> INSTRUMENTS() {
        return INSTRUMENTS;
    }

    public static CommandTarget<Material> MATERIALS() {
        return MATERIALS;
    }

    public static CommandTarget<Sound> SOUNDS() {
        return SOUNDS;
    }

    public static CommandTarget<Statistic> STATISTICS() {
        return STATISTICS;
    }

    public static CommandTarget<TreeType> TREE_TYPES() {
        return TREE_TYPES;
    }
}
