package com.ptsmods.chattix.config;

import com.ptsmods.chattix.util.Util;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.player.Player;
import org.hjson.JsonObject;

import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class ModerationConfig {
    public static ModerationConfig DEFAULT = new ModerationConfig(SlowModeConfig.DEFAULT, WelcomingConfig.DEFAULT, 1.0);
    private final SlowModeConfig slowModeConfig;
    private final WelcomingConfig welcomingConfig;
    private final double similarity;

    static ModerationConfig fromJson(JsonObject object) {
        SlowModeConfig slowModeConfig = object.get("slow_mode") == null ? SlowModeConfig.DEFAULT : SlowModeConfig.fromJson(object.get("slow_mode").asObject());
        WelcomingConfig welcomingConfig = object.get("welcoming") == null ? WelcomingConfig.DEFAULT : WelcomingConfig.fromJson(object.get("welcoming").asObject());
        return new ModerationConfig(slowModeConfig, welcomingConfig, object.getDouble("similarity", DEFAULT.getSimilarity()));
    }

    public boolean isTooSimilar(@NonNull String message, @NonNull String lastMessage) {
        int distance = Util.getLevenshteinDistance(message, lastMessage);
        double similarity = (message.length() - distance) / (double) message.length();

        return similarity >= this.similarity;
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    public static class SlowModeConfig {
        public static final SlowModeConfig DEFAULT = new SlowModeConfig(false, 3);
        private final Object2LongMap<UUID> lastSent = new Object2LongOpenHashMap<>();
        private final boolean enabled;
        private final int cooldown;

        static SlowModeConfig fromJson(JsonObject object) {
            return new SlowModeConfig(object.getBoolean("enabled", false), object.getInt("cooldown", DEFAULT.getCooldown()));
        }

        public boolean isOnCooldown(Player player) {
            return System.currentTimeMillis() - getLastSent(player) < cooldown * 1000L;
        }

        public long getLastSent(Player player) {
            return isEnabled() ? lastSent.getOrDefault(player.getUUID(), -1) : -1;
        }

        public void setLastSent(Player player) {
            lastSent.put(player.getUUID(), System.currentTimeMillis());
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    public static class WelcomingConfig {
        public static final WelcomingConfig DEFAULT = new WelcomingConfig(true, "<light_purple>Welcome to the server, %name%!</light_purple>", true);
        private final boolean enabled;
        private final String format;
        private final boolean broadcast;

        static WelcomingConfig fromJson(JsonObject object) {
            return new WelcomingConfig(object.getBoolean("enabled", true), object.getString("format", DEFAULT.getFormat()),
                    object.getBoolean("broadcast", true));
        }
    }
}
