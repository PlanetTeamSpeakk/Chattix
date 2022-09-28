package com.ptsmods.chattix.util.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class ChattixArchImpl {

    public static boolean hasPermission(CommandSourceStack stack, String permission) {
        return Permissions.check(stack, permission);
    }

    public static boolean hasPermission(ServerPlayer player, String permission) {
        return Permissions.check(player, permission);
    }

    public static void registerPermission(String permission, boolean defaultValue) {}
}
