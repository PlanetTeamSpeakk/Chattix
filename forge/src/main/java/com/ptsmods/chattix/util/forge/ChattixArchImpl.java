package com.ptsmods.chattix.util.forge;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ptsmods.chattix.forge.ChattixForge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;

public class ChattixArchImpl {
    public static boolean hasPermission(CommandSourceStack stack, String permission, boolean defaultValue) throws CommandSyntaxException {
        return PermissionAPI.getPermission(stack.getPlayerOrException(), ChattixForge.getPermission(permission));
    }

    public static boolean hasPermission(ServerPlayer player, String permission, boolean defaultValue) {
        return PermissionAPI.getPermission(player, ChattixForge.getPermission(permission));
    }

    public static void registerPermission(String permission, boolean defaultValue) {
        ChattixForge.registerPermission(permission, defaultValue);
    }
}
