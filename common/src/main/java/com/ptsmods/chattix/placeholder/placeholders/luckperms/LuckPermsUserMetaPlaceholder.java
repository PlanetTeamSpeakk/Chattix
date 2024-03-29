package com.ptsmods.chattix.placeholder.placeholders.luckperms;

import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.placeholder.StringPlaceholder;
import com.ptsmods.chattix.util.LPHelper;
import com.ptsmods.chattix.util.Util;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class LuckPermsUserMetaPlaceholder implements StringPlaceholder {
    @Override
    public boolean requiresArg() {
        return true;
    }

    @Override
    public String parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.isLuckPermsLoaded() && arg != null ? LPHelper.getUserData(player)
                .map(data -> data.getMetaData().getMetaValue(arg))
                .orElse("") : "";
    }
}
