package me.anjoismysign.skeramidcommands.throwable;

import me.anjoismysign.skeramidcommands.command.Command;

public class ChildNotAllowedException extends RuntimeException {

    private ChildNotAllowedException(Command parent, String child) {
        super("Attempted to add '" + child + "' to " + '"'
                + parent.getName() + '"' + " but was not possible due to '"
                + parent.getName() + "' having parameters.");
    }

    public static ChildNotAllowedException of(Command parent, String child) {
        return new ChildNotAllowedException(parent, child);
    }
}
