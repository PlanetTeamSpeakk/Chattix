package com.ptsmods.chattix.config;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.hjson.JsonObject;

import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class VicinityChatConfig {
    public static final VicinityChatConfig DEFAULT = new VicinityChatConfig(false, 100, LocalChatConfig.DEFAULT);
    private final boolean enabled;
    private final int radius;
    private final LocalChatConfig localChatConfig;

    static VicinityChatConfig fromJson(JsonObject object) {
        return new VicinityChatConfig(object.getBoolean("enabled", false), object.getInt("radius", 100),
                object.get("local_chat") == null ? LocalChatConfig.DEFAULT : LocalChatConfig.fromJson(object.get("local_chat").asObject()));
    }

    public List<ServerPlayer> filterRecipients(ServerPlayer player) {
        List<ServerPlayer> players = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayers();
        int max = radius * radius;
        boolean global = localChatConfig.isEnabled() && !localChatConfig.isEnabledFor(player);

        return !enabled ? ImmutableList.copyOf(players) : players.stream()
                .filter(recipient -> global || recipient.getLevel() == player.getLevel() && (radius <= 0 || recipient.distanceToSqr(player) < max))
                .toList();
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    public static class LocalChatConfig {
        public static final LocalChatConfig DEFAULT = new LocalChatConfig(true, true, "<dark_green><b>LOCAL</b></dark_green> ");

        private final boolean enabled;
        private final boolean isDefault;
        private final String prefix;
        private final Set<UUID> localChat = new HashSet<>();

        static LocalChatConfig fromJson(JsonObject object) {
            return new LocalChatConfig(object.getBoolean("enabled", true), object.getBoolean("default", true),
                    object.getString("prefix", DEFAULT.getPrefix()));
        }

        public boolean isEnabledFor(Player player) {
            return isEnabled() && isDefault() ^ localChat.contains(player.getUUID());
        }

        public void toggleFor(Player player) {
            if (!localChat.remove(player.getUUID())) localChat.add(player.getUUID());
        }
    }
}
