package io.github.anjoismysign.skeramidcommands.server.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class BukkitPermissionRegistry {

    public static void register(@NotNull String name, @NotNull String description) {
        if (name.isEmpty()) return;
        if (Bukkit.getPluginManager().getPermission(name) != null) return;

        Permission permission = new Permission(name, description, PermissionDefault.OP);
        Bukkit.getPluginManager().addPermission(permission);
    }

}