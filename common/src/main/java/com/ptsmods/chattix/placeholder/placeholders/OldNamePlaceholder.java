package com.ptsmods.chattix.placeholder.placeholders;

import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.placeholder.StringPlaceholder;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class OldNamePlaceholder implements StringPlaceholder {
    public static String currentOldName = null;

    @Override
    public String parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return currentOldName == null ? "%old_name%" : currentOldName;
    }
}
