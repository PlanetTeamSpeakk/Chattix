package com.ptsmods.chattix.util.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class ChattixArchImpl {

    public static boolean hasPermission(CommandSourceStack stack, String permission, boolean defaultValue) {
        return Permissions.check(stack, permission, defaultValue);
    }

    public static boolean hasPermission(ServerPlayer player, String permission, boolean defaultValue) {
        return Permissions.check(player, permission, defaultValue);
    }

    public static void registerPermission(String permission, boolean defaultValue) {}
}
