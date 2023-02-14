package com.ptsmods.chattix.placeholder;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record PlaceholderContext(ContextType type, ServerPlayer sender, @Nullable ServerPlayer receiver) {

    public enum ContextType {
        CHAT, MSG, BROADCAST
    }
}
