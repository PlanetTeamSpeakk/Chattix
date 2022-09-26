package com.ptsmods.chattix.placeholder.placeholders.luckperms;

import com.ptsmods.chattix.placeholder.Placeholder;
import com.ptsmods.chattix.util.LPHelper;
import com.ptsmods.chattix.util.Util;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class LuckPermsPrefixPlaceholder implements Placeholder {
    @Override
    public Component parse(@NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.isLuckPermsLoaded() ? LPHelper.getUserData(player)
                .map(data -> data.getMetaData().getPrefix())
                .map(LegacyComponentSerializer.legacyAmpersand()::deserialize)
                .orElse(net.kyori.adventure.text.Component.empty()) : net.kyori.adventure.text.Component.empty();
    }
}
