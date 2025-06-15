package io.github.anjoismysign.skeramidcommands.server.sponge;

import io.github.anjoismysign.skeramidcommands.server.PermissionMessenger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.service.permission.Subject;

public class SubjectAdapter implements PermissionMessenger {
    private final Subject subject;
    private final Audience audience;

    protected SubjectAdapter(Subject subject, Audience audience) {
        this.subject = subject;
        this.audience = audience;
    }

    public boolean hasPermission(String permission) {
        return subject.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        audience.sendMessage(LegacyComponentSerializer.legacySection()
                .deserialize(message));
    }

    public Subject getSubject() {
        return subject;
    }
}
