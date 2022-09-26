package com.ptsmods.chattix.placeholder;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Placeholder {

    default boolean requiresArg() {
        return false;
    }

    Component parse(@NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg);
}
