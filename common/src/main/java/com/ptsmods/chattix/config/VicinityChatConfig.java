package com.ptsmods.chattix.config;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import org.hjson.JsonObject;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class VicinityChatConfig {
    public static final VicinityChatConfig DEFAULT = new VicinityChatConfig(false, 100);
    private final boolean enabled;
    private final int radius;

    static VicinityChatConfig fromJson(JsonObject object) {
        return new VicinityChatConfig(object.getBoolean("enabled", false), object.getInt("radius", 100));
    }

    public List<ServerPlayer> filterRecipients(ServerPlayer player) {
        List<ServerPlayer> players = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayers();
        int max = radius * radius;

        return !enabled ? ImmutableList.copyOf(players) : players.stream()
                .filter(recipient -> recipient.getLevel() == player.getLevel() && (radius <= 0 || recipient.distanceToSqr(player) < max))
                .toList();
    }
}
