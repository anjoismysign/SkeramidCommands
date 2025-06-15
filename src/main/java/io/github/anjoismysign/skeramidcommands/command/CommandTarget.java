package io.github.anjoismysign.skeramidcommands.command;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandTarget<T> {
    List<String> get();

    @Nullable
    T parse(String arg);
}
