package com.ptsmods.chattix.util;

import dev.architectury.platform.Platform;
import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Optional;

@UtilityClass
public class Util {
    public static boolean isLuckPermsLoaded() {
        return Platform.isModLoaded("luckperms");
    }

    public static Optional<PlayerTeam> getTeam(Player player) {
        return Optional.ofNullable(player)
                .map(Player::getTeam)
                .map(team -> (PlayerTeam) team);
    }
}
