package com.ptsmods.chattix.util.forge;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ptsmods.chattix.forge.ChattixForge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.server.permission.PermissionAPI;

public class ChattixArchImpl {
    public static boolean hasPermission(CommandSourceStack stack, String permission) throws CommandSyntaxException {
        return PermissionAPI.getPermission(stack.getPlayerOrException(), ChattixForge.getPermission(permission));
    }

    public static void registerPermission(String permission, boolean defaultValue) {
        ChattixForge.registerPermission(permission, defaultValue);
    }
}
