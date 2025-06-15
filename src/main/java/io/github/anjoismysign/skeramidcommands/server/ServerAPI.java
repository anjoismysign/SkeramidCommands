package io.github.anjoismysign.skeramidcommands.server;

public enum ServerAPI {
    BUKKIT("org.bukkit.command.CommandSender"),
    SPONGE("org.spongepowered.api.service.permission.Subject");

    private final String implementationPath;

    ServerAPI(String checkClass) {
        this.implementationPath = checkClass;
    }

    public String getImplementationPath() {
        return implementationPath;
    }
}
