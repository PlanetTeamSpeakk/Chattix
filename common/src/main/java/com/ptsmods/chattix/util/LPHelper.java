package com.ptsmods.chattix.util;

import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

@UtilityClass
public class LPHelper {
    public LuckPerms getLuckPerms() {
        return LuckPermsProvider.get();
    }

    public Optional<Group> getGroup(String name) {
        return Optional.ofNullable(getLuckPerms().getGroupManager().getGroup(name));
    }

    public Optional<User> getUser(Player player) {
        return Optional.ofNullable(getLuckPerms().getUserManager().getUser(player.getUUID()));
    }

    public Optional<CachedDataManager> getUserData(Player player) {
        return getUser(player).map(PermissionHolder::getCachedData);
    }

    public boolean hasPermission(Player player, String permission) {
        return getUserData(player)
                .map(user -> user.getPermissionData().checkPermission(permission).asBoolean())
                .orElse(true);
    }
}
