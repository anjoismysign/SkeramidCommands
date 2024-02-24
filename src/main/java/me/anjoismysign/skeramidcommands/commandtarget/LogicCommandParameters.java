package me.anjoismysign.skeramidcommands.commandtarget;

import me.anjoismysign.skeramidcommands.command.CommandTarget;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogicCommandParameters {

    static private final Map<Integer, List<String>> map;
    private static final CommandTarget<Integer> INTEGER = new CommandTarget<Integer>() {
        @Override
        public List<String> get() {
            return map.get(0);
        }

        @Override
        public @Nullable Integer parse(String arg) {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    private static final CommandTarget<Integer> POSITIVE_INTEGER = new CommandTarget<Integer>() {
        @Override
        public List<String> get() {
            return map.get(1);
        }

        @Override
        public @Nullable Integer parse(String arg) {
            try {
                int i = Integer.parseInt(arg);
                return i > 0 ? i : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    private static final CommandTarget<Integer> NEGATIVE_INTEGER = new CommandTarget<Integer>() {
        @Override
        public List<String> get() {
            return map.get(2);
        }

        @Override
        public @Nullable Integer parse(String arg) {
            try {
                int i = Integer.parseInt(arg);
                return i < 0 ? i : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    private static final CommandTarget<Byte> BYTE = new CommandTarget<Byte>() {
        @Override
        public List<String> get() {
            return map.get(3);
        }

        @Override
        public @Nullable Byte parse(String arg) {
            try {
                return Byte.parseByte(arg);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    private static final CommandTarget<Byte> POSITIVE_BYTE = new CommandTarget<Byte>() {
        @Override
        public List<String> get() {
            return map.get(4);
        }

        @Override
        public @Nullable Byte parse(String arg) {
            try {
                byte i = Byte.parseByte(arg);
                return i > 0 ? i : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    private static final CommandTarget<Byte> NEGATIVE_BYTE = new CommandTarget<Byte>() {
        @Override
        public List<String> get() {
            return map.get(5);
        }

        @Override
        public @Nullable Byte parse(String arg) {
            try {
                byte i = Byte.parseByte(arg);
                return i < 0 ? i : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    private static final CommandTarget<Boolean> BOOLEAN = new CommandTarget<Boolean>() {
        @Override
        public List<String> get() {
            return map.get(6);
        }

        @Override
        public @Nullable Boolean parse(String arg) {
            return arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false") ? Boolean.parseBoolean(arg) : null;
        }
    };

    static {
        map = new HashMap<>();
        map.put(0, Arrays.asList("-2147483648", "0", "1", "2", "3", "2147483647"));
        map.put(1, Arrays.asList("0", "1", "2", "3", "2147483647"));
        map.put(2, Arrays.asList("-2147483648", "-3", "-2", "-1", "0"));
        map.put(3, Arrays.asList("-128", "0", "1", "2", "3", "127"));
        map.put(4, Arrays.asList("0", "1", "2", "3", "127"));
        map.put(5, Arrays.asList("-128", "-3", "-2", "-1", "0"));
        map.put(6, Arrays.asList("true", "false"));
    }

    public static CommandTarget<Integer> INTEGER() {
        return INTEGER;
    }

    public static CommandTarget<Integer> POSITIVE_INTEGER() {
        return POSITIVE_INTEGER;
    }

    public static CommandTarget<Integer> NEGATIVE_INTEGER() {
        return NEGATIVE_INTEGER;
    }

    public static CommandTarget<Byte> BYTE() {
        return BYTE;
    }

    public static CommandTarget<Byte> POSITIVE_BYTE() {
        return POSITIVE_BYTE;
    }

    public static CommandTarget<Byte> NEGATIVE_BYTE() {
        return NEGATIVE_BYTE;
    }

    public static CommandTarget<Boolean> BOOLEAN() {
        return BOOLEAN;
    }
}