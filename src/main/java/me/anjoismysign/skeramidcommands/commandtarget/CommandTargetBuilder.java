package me.anjoismysign.skeramidcommands.commandtarget;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.anjoismysign.skeramidcommands.command.CommandTarget;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class CommandTargetBuilder {
    public static <T extends Enum<T>> CommandTarget<T> fromEnum(Class<T> enumClass) {
        return new CommandTarget<T>() {
            private final List<String> enumValues = new ArrayList<>();

            {
                for (T enumValue : enumClass.getEnumConstants()) {
                    enumValues.add(enumValue.name());
                }
            }

            @Override
            public List<String> get() {
                return enumValues;
            }

            @Override
            public T parse(String arg) {
                try {
                    return Enum.valueOf(enumClass, arg);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        };
    }

    public static <T> CommandTarget<T> fromMap(Supplier<Map<String, T>> supplier) {
        return new CommandTarget<T>() {
            @Override
            public List<String> get() {
                return new ArrayList<>(supplier.get().keySet());
            }

            @Override
            public T parse(String arg) {
                return supplier.get().get(arg);
            }
        };
    }

    public static <T extends Keyed> CommandTarget<T> OF_REGISTRY_KEY(@NotNull RegistryKey<T> registryKey) {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        final Registry<T> registry = registryAccess.getRegistry(registryKey);
        Objects.requireNonNull(registry, "\"" + registryKey.key().asString() + "\" has no Paper Registry!");
        return new CommandTarget<T>() {
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
