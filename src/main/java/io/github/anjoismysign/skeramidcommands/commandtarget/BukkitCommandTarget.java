package io.github.anjoismysign.skeramidcommands.commandtarget;

import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public static <T extends Keyed> CommandTarget<T> OF_REGISTRY_KEY(@NotNull RegistryKey<T> registryKey) {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        final Registry<@NotNull T> registry = registryAccess.getRegistry(registryKey);
        Objects.requireNonNull(registry, "\"" + registryKey.key().asString() + "\" has no Paper Registry!");
        return new CommandTarget<>() {
            private final List<String> keys = new ArrayList<>();

            {
                registry.forEach(keyedClass -> {
                    String name = keyedClass.getKey().getKey();
                    this.keys.add(name);
                });
            }

            @Override
            public List<String> get() {
                return this.keys;
            }

            public T parse(String arg) {
                return registry.get(NamespacedKey.minecraft(arg));
            }
        };
    }
}
