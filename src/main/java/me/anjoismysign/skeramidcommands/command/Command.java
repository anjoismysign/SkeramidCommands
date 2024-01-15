package me.anjoismysign.skeramidcommands.command;

import me.anjoismysign.skeramid.Skeramid;
import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface Command extends Skeramid<Command> {

    /**
     * Will be called when the command is executed.
     *
     * @param consumer the consumer.
     */
    void onExecute(Consumer<PermissionMessenger> consumer);

    /**
     * Will run logic with the given CommandSender since the sender is authorized.
     *
     * @param permissionMessenger the sender.
     */
    boolean run(PermissionMessenger permissionMessenger);

    /**
     * Checks whether the sender is authorized to execute the command
     *
     * @param permissionMessenger the sender
     * @return true if the sender is authorized, false otherwise
     */
    boolean isAuthorized(PermissionMessenger permissionMessenger);

    /**
     * Returns the name of the command.
     *
     * @return the name.
     */
    @NotNull
    String getName();

    /**
     * Gets the alias of the command.
     *
     * @return the alias.
     */
    @NotNull
    List<String> getAlias();

    /**
     * Gets the permission of the command.
     *
     * @return the permission.
     */
    @NotNull
    String getPermission();

    /**
     * Gets the description of the command.
     *
     * @return the description.
     */
    @NotNull
    String getDescription();

    /**
     * Gets a string representation of the command.
     *
     * @return the string representation.
     */
    @NotNull
    String toString();

    /**
     * Gets a child command by name and alias.
     *
     * @param name  the name.
     * @param alias the alias.
     * @return the child command.
     */
    default Command child(String name, List<String> alias) {
        Command child = child(name);
        child.getAlias().addAll(alias);
        return child;
    }

    /**
     * Gets a child command by name and alias.
     *
     * @param name  the name.
     * @param alias the alias.
     * @return the child command.
     */
    default Command child(String name, String... alias) {
        Command child = child(name);
        for (String s : alias) {
            child.getAlias().add(s);
        }
        return child;
    }
}
