package com.ptsmods.chattix.placeholder.placeholders.luckperms;

import com.ptsmods.chattix.placeholder.Placeholder;
import com.ptsmods.chattix.util.LPHelper;
import com.ptsmods.chattix.util.Util;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class LuckPermsUserMetaPlaceholder implements Placeholder {
    @Override
    public boolean requiresArg() {
        return true;
    }

    @Override
    public Component parse(@NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.isLuckPermsLoaded() && arg != null ? LPHelper.getUserData(player)
                .map(data -> data.getMetaData().getMetaValue(arg))
                .map(Component::text)
                .orElse(Component.empty()) : Component.empty();
    }
}
