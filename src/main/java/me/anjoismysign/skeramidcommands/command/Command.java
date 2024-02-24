package me.anjoismysign.skeramidcommands.command;

import me.anjoismysign.skeramid.Skeramid;
import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;

public interface Command extends Skeramid<Command> {

    /**
     * Gets the parameters of the command.
     *
     * @return the parameters. null if the command has no parameters.
     */
    @NotNull
    List<CommandTarget<?>> getParameters();

    /**
     * Sets the parameters of the command.
     *
     * @param targets the parameters.
     */
    void setParameters(CommandTarget<?>... targets);

    /**
     * Checks whether the command has parameters.
     *
     * @return true if the command has parameters, false otherwise.
     */
    default boolean hasParameters() {
        return !getParameters().isEmpty();
    }

    /**
     * Will be called when the command is executed.
     *
     * @param consumer the consumer.
     */
    void onExecute(BiConsumer<PermissionMessenger, String[]> consumer);

    /**
     * Will run logic with the given CommandSender since the sender is authorized.
     *
     * @param permissionMessenger the sender.
     */
    boolean run(PermissionMessenger permissionMessenger, String... args);

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
}
