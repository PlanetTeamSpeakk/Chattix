package com.ptsmods.chattix.placeholder;

import com.google.common.collect.ImmutableMap;
import com.ptsmods.chattix.placeholder.placeholders.*;
import com.ptsmods.chattix.placeholder.placeholders.luckperms.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.minecraft.server.level.ServerPlayer;
import oshi.util.tuples.Pair;

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@UtilityClass
public class Placeholders {
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(\\\\)?(%([A-Za-z_\\-]*):?([A-Za-z_\\-]*?)%)");
    private final Map<String, Placeholder<?>> placeholders = ImmutableMap.<String, Placeholder<?>>builder()
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

    public String parseStringPlaceholders(String s, PlaceholderContext context, ServerPlayer player) {
        return PLACEHOLDER_PATTERN.matcher(s).replaceAll(res -> {
            Pair<Placeholder<?>, ServerPlayer> pair = getPlaceholder(res, context, player);
            if (pair == null) return res.group(2);
            if (!(pair.getA() instanceof StringPlaceholder placeholder)) return res.group();

            String arg = res.group(4);
            if (placeholder.requiresArg() && arg == null) return res.group(2);

            return placeholder.parse(context, pair.getB(), Component.empty(), arg);
        });
    }

    public TextReplacementConfig createReplacementConfig(PlaceholderContext context, ServerPlayer player, Component message) {
        return TextReplacementConfig.builder()
                .match(PLACEHOLDER_PATTERN)
                .replacement((res, builder) -> {
                    Pair<Placeholder<?>, ServerPlayer> pair = getPlaceholder(res, context, player);
                    if (pair == null) return Component.text(res.group(2));
                    if (!(pair.getA() instanceof ComponentPlaceholder placeholder)) return Component.text(res.group());

                    String arg = res.group(4);
                    if (placeholder.requiresArg() && arg == null) return Component.text(res.group(2));

                    return placeholder.parse(context, pair.getB(), message, arg);
                })
                .build();
    }

    private Pair<Placeholder<?>, ServerPlayer> getPlaceholder(MatchResult res, PlaceholderContext context, ServerPlayer player) {
        String plName = res.group(3).toLowerCase();
        ServerPlayer target = plName.startsWith("sender_") ? context.sender() : plName.startsWith("recipient_") ? context.receiver() : player;
        plName = plName.startsWith("sender_") || plName.startsWith("recipient_") ?
                plName.substring(plName.indexOf('_') + 1) : plName;

        if (target == null || res.group(1) != null || !placeholders.containsKey(plName))
            return null;

        return new Pair<>(placeholders.get(plName), target);
    }
}
