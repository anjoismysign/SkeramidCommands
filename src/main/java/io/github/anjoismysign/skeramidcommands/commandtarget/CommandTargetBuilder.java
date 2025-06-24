package io.github.anjoismysign.skeramidcommands.commandtarget;

import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
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
}
