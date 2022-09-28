package com.ptsmods.chattix.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class ChattixArch {

    @ExpectPlatform
    public static boolean hasPermission(CommandSourceStack stack, String permission) {
        throw new AssertionError("This shouldn't happen.");
    }

    @ExpectPlatform
    public static boolean hasPermission(ServerPlayer player, String permission) {
        throw new AssertionError("This shouldn't happen.");
    }

    @ExpectPlatform
    public static void registerPermission(String permission, boolean defaultValue) {}
}
