package com.ptsmods.chattix.placeholder.placeholders.luckperms;

import com.ptsmods.chattix.placeholder.Placeholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.util.LPHelper;
import com.ptsmods.chattix.util.Util;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class LuckPermsGroupMetaPlaceholder implements Placeholder {
    @Override
    public boolean requiresArg() {
        return true;
    }

    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.isLuckPermsLoaded() && arg != null ? LPHelper.getUser(player)
                .flatMap(user -> LPHelper.getGroup(user.getPrimaryGroup()))
                .map(group -> group.getCachedData().getMetaData().getMetaValue(arg))
                .map(Component::text)
                .orElse(Component.empty()) : Component.empty();
    }
}
