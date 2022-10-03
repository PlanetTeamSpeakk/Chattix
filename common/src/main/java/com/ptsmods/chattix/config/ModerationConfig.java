package com.ptsmods.chattix.config;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.player.Player;
import org.hjson.JsonObject;

import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class ModerationConfig {
    public static ModerationConfig DEFAULT = new ModerationConfig(SlowModeConfig.DEFAULT);
    private final SlowModeConfig slowModeConfig;

    static ModerationConfig fromJson(JsonObject object) {
        SlowModeConfig slowModeConfig = object.get("slow_mode") == null ? SlowModeConfig.DEFAULT : SlowModeConfig.fromJson(object.get("slow_mode").asObject());
        return new ModerationConfig(slowModeConfig);
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
}
