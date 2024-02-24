package me.anjoismysign.skeramidcommands.command;

import java.util.List;

public interface KeyedCommandTarget extends CommandTarget {
    List<String> get(String key);
}
