package com.ptsmods.chattix.placeholder.placeholders;

import com.ptsmods.chattix.placeholder.ComponentPlaceholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.util.Util;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

public class TeamNamePlaceholder implements ComponentPlaceholder {
    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return Util.getTeam(player)
                .map(Team::getName)
                .map(net.kyori.adventure.text.Component::text)
                .orElse(net.kyori.adventure.text.Component.empty());
    }
}
