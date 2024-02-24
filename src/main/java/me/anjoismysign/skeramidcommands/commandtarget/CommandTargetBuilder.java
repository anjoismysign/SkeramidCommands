package me.anjoismysign.skeramidcommands.commandtarget;

import me.anjoismysign.skeramidcommands.command.CommandTarget;

import java.util.ArrayList;
import java.util.List;

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
}
