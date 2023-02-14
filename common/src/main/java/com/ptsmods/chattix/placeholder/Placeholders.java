package com.ptsmods.chattix.placeholder;

import com.google.common.collect.ImmutableMap;
import com.ptsmods.chattix.placeholder.placeholders.*;
import com.ptsmods.chattix.placeholder.placeholders.luckperms.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.regex.Pattern;

@UtilityClass
public class Placeholders {
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(\\\\)?(%([A-Za-z_\\-]*):?([A-Za-z_\\-]*?)%)");
    private final Map<String, Placeholder> placeholders = ImmutableMap.<String, Placeholder>builder()
            .put("message", new MessagePlaceholder())
            .put("luckperms_user_meta", new LuckPermsUserMetaPlaceholder())
            .put("luckperms_group_meta", new LuckPermsGroupMetaPlaceholder())
            .put("luckperms_group", new LuckPermsGroupPlaceholder())
            .put("luckperms_prefix", new LuckPermsPrefixPlaceholder())
            .put("luckperms_suffix", new LuckPermsSuffixPlaceholder())
            .put("team_name", new TeamNamePlaceholder())
            .put("team_prefix", new TeamPrefixPlaceholder())
            .put("team_suffix", new TeamSuffixPlaceholder())
            .put("displayname", new DisplayNamePlaceholder())
            .put("name", new NamePlaceholder())
            .put("old_name", new OldNamePlaceholder()) // Only valid in join changed name format
            .put("world", new WorldPlaceholder())
            .build();

    public static void init() {} // Init fields and load placeholder classes.

    public TextReplacementConfig createReplacementConfig(PlaceholderContext context, ServerPlayer player, Component message) {
        return TextReplacementConfig.builder()
                .match(PLACEHOLDER_PATTERN)
                .replacement((res, builder) -> {
                    String plName = res.group(3).toLowerCase();
                    ServerPlayer target = plName.startsWith("sender_") ? context.sender() : plName.startsWith("recipient_") ? context.receiver() : player;
                    plName = plName.startsWith("sender_") || plName.startsWith("recipient_") ?
                            plName.substring(plName.indexOf('_') + 1) : plName;

                    if (target == null || res.group(1) != null || !placeholders.containsKey(plName))
                        return Component.text(res.group(2));

                    Placeholder placeholder = placeholders.get(plName);
                    if (placeholder.requiresArg() && res.group(4) == null) return Component.text(res.group(2));

                    return placeholder.parse(context, target, message, res.group(4));
                })
                .build();
    }
}
