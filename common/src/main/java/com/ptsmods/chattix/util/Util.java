package com.ptsmods.chattix.util;

import dev.architectury.platform.Platform;
import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Arrays;
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

    // Levenshtein distance source: https://www.geeksforgeeks.org/java-program-to-implement-levenshtein-distance-computing-algorithm/
    public static int getLevenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
            for (int j = 0; j <= str2.length(); j++)
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else dp[i][j] = minEdits(dp[i - 1][j - 1] + replacementCount(str1.charAt(i - 1), str2.charAt(j - 1)),
                            dp[i - 1][j] + 1, dp[i][j - 1] + 1);

        return dp[str1.length()][str2.length()];
    }

    private static int replacementCount(char c1, char c2) {
        return c1 == c2 ? 0 : 1;
    }

    private static int minEdits(int... nums) {
        return Arrays.stream(nums).min().orElse(Integer.MAX_VALUE);
    }
}
