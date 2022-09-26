package com.ptsmods.chattix.placeholder.placeholders;

import com.ptsmods.chattix.placeholder.Placeholder;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class MessagePlaceholder implements Placeholder {
    @Override
    public Component parse(@NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return message;
    }
}
