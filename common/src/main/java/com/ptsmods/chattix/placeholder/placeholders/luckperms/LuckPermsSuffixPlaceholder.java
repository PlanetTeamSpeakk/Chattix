package com.ptsmods.chattix.placeholder.placeholders.luckperms;

import com.ptsmods.chattix.placeholder.ComponentPlaceholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.util.LPHelper;
import com.ptsmods.chattix.util.Util;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class LuckPermsSuffixPlaceholder implements ComponentPlaceholder {
    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.isLuckPermsLoaded() ? LPHelper.getUserData(player)
                .map(data -> data.getMetaData().getSuffix())
                .map(LegacyComponentSerializer.legacyAmpersand()::deserialize)
                .orElse(Component.empty()) : Component.empty();
    }
}
