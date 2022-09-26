package com.ptsmods.chattix.config;

import com.google.common.collect.ImmutableMap;
import com.ptsmods.chattix.util.LPHelper;
import com.ptsmods.chattix.util.Util;
import it.unimi.dsi.fastutil.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class FormattingConfig {
    public static final FormattingConfig DEFAULT = new FormattingConfig(true, "%luckperms_prefix%%displayname%%luckperms_suffix%: %message%",
            GroupConfig.DEFAULT, ImmutableMap.of());
    private final boolean enabled;
    @NonNull private final String format;
    @NonNull private final GroupConfig groupConfig;
    @NonNull private final Map<ResourceLocation, String> worldConfig;

    static FormattingConfig fromJson(JsonObject object) {
        boolean enabled = object.getBoolean("enabled", true);
        String format = object.getString("format", DEFAULT.getFormat());
        GroupConfig groupConfig = object.get("groups") == null ? GroupConfig.DEFAULT : GroupConfig.fromJson(object.get("groups").asObject());
        Map<ResourceLocation, String> worldConfig = object.get("worlds") == null ? ImmutableMap.of() :
                StreamSupport.stream(object.get("worlds").asObject().spliterator(), false)
                        .map(member -> Pair.of(new ResourceLocation(member.getName()), member.getValue().asString()))
                        .collect(ImmutableMap.toImmutableMap(Pair::left, Pair::right));

        return new FormattingConfig(enabled, format, groupConfig, worldConfig);
    }

    public String getActiveFormatFor(ServerPlayer player) {
        if (player == null) return format;

        ResourceLocation dimension = player.getLevel().dimension().location();
        Optional<GroupConfig.Group> group = getGroupsFor(player).stream()
                .findFirst();
        if (group.isEmpty()) return worldConfig.getOrDefault(dimension, format);

        // Priority:
        // 1. Group-specific and world-specific
        // 2. World-specific
        // 3. Group-specific
        // 4. Global default
        return group.get().getFormats().stream()
                .filter(pair -> pair.left() != null && pair.left().equals(dimension))
                .findFirst()
                .map(Pair::right)
                .or(() -> Optional.ofNullable(worldConfig.get(dimension)))
                .or(() -> group.get().getFormats().stream()
                        .filter(pair -> pair.left() == null)
                        .findFirst()
                        .map(Pair::right))
                .orElse(format);
    }

    public List<GroupConfig.Group> getGroupsFor(ServerPlayer player) {
        return groupConfig.isEnabled() && Util.isLuckPermsLoaded() ? groupConfig.getGroups().stream()
                .filter(group -> LPHelper.hasPermission(player, group.getPermission()))
                .toList() : Collections.emptyList();
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    public static class GroupConfig {
        private static final GroupConfig DEFAULT = new GroupConfig(false, Collections.emptyList());
        private final boolean enabled;
        @NonNull private final List<Group> groups;

        private static GroupConfig fromJson(JsonObject object) {
            boolean enabled = object.getBoolean("enabled", false);
            List<Group> groups = StreamSupport.stream(object.spliterator(), false)
                    .filter(member -> !"enabled".equals(member.getName()))
                    .map(member -> Group.fromJson(member.getName(), member.getValue()))
                    .toList();

            return new GroupConfig(enabled, groups);
        }

        @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
        @Getter
        public static class Group {
            @NonNull private final String name;
            @NonNull private final List<Pair<ResourceLocation, String>> formats;

            private static Group fromJson(String name, JsonValue value) {
                return value.isString() ? new Group(name, Collections.singletonList(Pair.of(null, value.asString()))) :
                        new Group(name, StreamSupport.stream(value.asArray().spliterator(), false)
                                .map(JsonValue::asObject)
                                .map(entry -> Pair.of(Optional.ofNullable(entry.getString("world", null))
                                                .map(ResourceLocation::new)
                                                .orElse(null), entry.getString("format", FormattingConfig.DEFAULT.getFormat())))
                                .toList());
            }

            public String getPermission() {
                return "chattix.group." + name;
            }
        }
    }
}
