package com.ptsmods.chattix.placeholder.placeholders.luckperms;

import com.ptsmods.chattix.placeholder.ComponentPlaceholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.util.LPHelper;
import com.ptsmods.chattix.util.Util;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class LuckPermsGroupPlaceholder implements ComponentPlaceholder {
    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.isLuckPermsLoaded() ? LPHelper.getUser(player)
                .map(User::getPrimaryGroup)
                .flatMap(LPHelper::getGroup)
                .map(group -> group.getDisplayName() == null ? Component.text(group.getName()) :
                        LegacyComponentSerializer.legacyAmpersand().deserialize(group.getDisplayName()))
                .orElse(Component.empty()) : Component.empty();
    }
}
