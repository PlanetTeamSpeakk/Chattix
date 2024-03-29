package com.ptsmods.chattix.placeholder.placeholders;

import com.ptsmods.chattix.placeholder.ComponentPlaceholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.util.Util;
import com.ptsmods.chattix.util.VanillaComponentSerializer;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public class TeamPrefixPlaceholder implements ComponentPlaceholder {
    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.getTeam(player)
                .map(PlayerTeam::getPlayerPrefix)
                .map(VanillaComponentSerializer.vanilla()::deserialize)
                .orElse(net.kyori.adventure.text.Component.empty());
    }
}
