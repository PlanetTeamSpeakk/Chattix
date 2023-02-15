package com.ptsmods.chattix.placeholder;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public sealed interface Placeholder<R> permits ComponentPlaceholder, StringPlaceholder {

    default boolean requiresArg() {
        return false;
    }

    R parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg);
}
