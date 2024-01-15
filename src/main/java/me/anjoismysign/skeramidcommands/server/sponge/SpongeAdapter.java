package me.anjoismysign.skeramidcommands.server.sponge;

import me.anjoismysign.skeramidcommands.command.Command;
import me.anjoismysign.skeramidcommands.server.PermissionMessenger;
import me.anjoismysign.skeramidcommands.server.ServerAPI;
import me.anjoismysign.skeramidcommands.server.ServerAdapter;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.service.permission.Subject;

public class SpongeAdapter implements ServerAdapter<Subject> {
    private static SpongeAdapter instance;

    public static SpongeAdapter getInstance() {
        if (instance == null) {
            instance = new SpongeAdapter();
        }
        return instance;
    }

    @NotNull
    public Subject of(PermissionMessenger permissionMessenger) {
        return unadapt(permissionMessenger).getSubject();
    }

    /**
     * Unadapts a PermissionMessenger to a SubjectAdapter.
     *
     * @param permissionMessenger the PermissionMessenger.
     * @return the SubjectAdapter.
     */
    @NotNull
    public SubjectAdapter unadapt(PermissionMessenger permissionMessenger) {
        if (permissionMessenger instanceof SubjectAdapter) {
            return (SubjectAdapter) permissionMessenger;
        }
        throw new IllegalArgumentException("'permissionMessenger' is not a SubjectAdapter");
    }

    @NotNull
    public SubjectAdapter adapt(Object object) {
        if (object instanceof Subject && object instanceof Audience) {
            return new SubjectAdapter((Subject) object, (Audience) object);
        }
        throw new IllegalArgumentException("Object is not a Subject");
    }


    @NotNull
    public ServerAPI getServerAPI() {
        return ServerAPI.SPONGE;
    }

    @NotNull
    public Command createCommand(@NotNull String name, @NotNull String permission, @NotNull String description) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
