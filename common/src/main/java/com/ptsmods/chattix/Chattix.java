package com.ptsmods.chattix;

import com.ptsmods.chattix.commands.*;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.config.FilteringConfig;
import com.ptsmods.chattix.config.ModerationConfig;
import com.ptsmods.chattix.config.VicinityChatConfig;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.placeholder.Placeholders;
import com.ptsmods.chattix.util.ChattixArch;
import com.ptsmods.chattix.util.VanillaComponentSerializer;
import com.ptsmods.chattix.util.addons.ServerPlayerAddon;
import dev.architectury.event.events.common.ChatEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

public class Chattix {
    public static final String MOD_ID = "chattix";
    public static final Logger LOG = LogManager.getLogger();
    private static final Path chatDisabledFile = Config.getConfigFile().resolveSibling("chat_disabled");
    private static final List<BiFunction<ServerPlayer, String, MutableComponent>> predicates = new ArrayList<>();
    private static final Map<UUID, String> lastMessages = new HashMap<>();
    private static boolean formattingMessageArgument = false;
    @Getter
    private static boolean chatDisabled = Files.exists(chatDisabledFile);

    public static void init() {
        Config.load();

        // Workaround for a bug (I think) in Forge.
        // Currently, whenever you try to load classes in an event (or perhaps specifically async ones), the wrong
        // classloader is used. Instead of the classloader that loaded this mod file, the built-in system classloader is
        // used which does not know about this mod file nor its contents and thus cannot find its classes.
        // To work around this, we load all necessary classes by formatting an empty message beforehand.
        format(null, net.kyori.adventure.text.Component.empty());
        Placeholders.init(); // We call this for the same reason.

        ChattixArch.registerPermission("chattix.bypass", false);
        ChattixArch.registerPermission("chattix.links", false);

        //noinspection ConstantConditions
        predicates.add((player, msg) -> chatDisabled && !ChattixArch.hasPermission(player, "chattix.bypass", false) ?
                Component.literal("Chat is currently disabled!") : null);
        predicates.add((player, msg) -> Config.getInstance().isMuted(player) ? Component.literal("You cannot speak as you are muted! Reason: ")
                .append(Config.getInstance().getMuteReason(player)) : null);
        predicates.add((player, msg) -> {
            if (ChattixArch.hasPermission(player, "chattix.bypass", false)) return null;

            ModerationConfig.SlowModeConfig slowModeConfig = Config.getInstance().getModerationConfig().getSlowModeConfig();
            int remaining = (int) (slowModeConfig.getCooldown() - (System.currentTimeMillis() - slowModeConfig.getLastSent(player)) / 1000);
            return slowModeConfig.isOnCooldown(player) ?
                    Component.literal("Too fast! Slow mode is enabled and you need to wait " +
                            remaining + " more second" + (remaining == 1 ? "" : "s") + ".") : null;
        });
        predicates.add((player, msg) -> !ChattixArch.hasPermission(player, "chattix.bypass", false) &&
                Config.getInstance().getModerationConfig().isTooSimilar(msg, getLastMessage(player)) ?
                Component.literal("That message is too similar to your last message!") : null);

        ChatEvent.DECORATE.register((player, component) -> {
            if (player == null) return;

            MutableComponent errorMsg = getErrorMsg(player, component.get().getString());
            if (errorMsg != null) {
                component.set(errorMsg);
                return;
            }

            if (!formattingMessageArgument && Config.getInstance().getFormattingConfig().isEnabled())
                component.set(format(player, VanillaComponentSerializer.vanilla().deserialize(component.get())));
        });

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            ChattixCommand.register(dispatcher);
            ChatCommand.register(dispatcher);

            if (selection != Commands.CommandSelection.DEDICATED) return;
            IgnoreCommand.register(dispatcher);
            MentionsCommand.register(dispatcher);
            MuteCommand.register(dispatcher);
            BroadcastCommand.register(dispatcher);
            LocalCommand.register(dispatcher);
            ReplyCommand.register(dispatcher);
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            ModerationConfig.WelcomingConfig welcomingConfig = Config.getInstance().getModerationConfig().getWelcomingConfig();
            if (!welcomingConfig.isEnabled() || welcomingConfig.getFormat().isEmpty() || !((ServerPlayerAddon) player).isFirstTimePlaying()) return;

            Component message = format(player, net.kyori.adventure.text.Component.empty(), welcomingConfig.getFormat(), false, true);
            if (welcomingConfig.isBroadcast()) Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(message, false);
            else player.sendSystemMessage(message);
        });

        PlayerEvent.PLAYER_JOIN.register(player -> Config.getInstance().getIgnored().refresh(player.getUUID()));
        PlayerEvent.PLAYER_QUIT.register(player -> Config.getInstance().getIgnored().invalidate(player.getUUID()));
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message) {
        return format(player, message, Config.getInstance().getFormattingConfig().getActiveFormatFor(player), true);
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message, String format, boolean filter) {
        return format(player, message, format, filter, false);
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message, String format, boolean filter, boolean ignoreLocal) {
        return format(player, message, format, new PlaceholderContext(PlaceholderContext.ContextType.CHAT, player, null), filter, ignoreLocal);
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message, String format, PlaceholderContext placeholderContext, boolean filter, boolean ignoreLocal) {
        //noinspection ConstantConditions
        if (filter && (player == null || !ChattixArch.hasPermission(player, "chattix.bypass", false)))
            message = filter(message).right();

        VicinityChatConfig vicinityChatConfig = Config.getInstance().getVicinityChatConfig();
        TextReplacementConfig placeholderReplacement = Placeholders.createReplacementConfig(placeholderContext, player, message);

        net.kyori.adventure.text.Component adventureOutput = createMiniMessage(placeholderReplacement)
                .deserialize(format).replaceText(placeholderReplacement);

        ModerationConfig.LinksConfig linksConfig = Config.getInstance().getModerationConfig().getLinksConfig();
        //noinspection ConstantValue
        if (linksConfig.isEnabled() && (!linksConfig.isRequiresPermission() || player == null ||
                ChattixArch.hasPermission(player, "chattix.links", false)))
            adventureOutput = adventureOutput.replaceText(linksConfig.getReplacementConfig());

        Component output = VanillaComponentSerializer.vanilla().serialize(adventureOutput);
        return player != null && !ignoreLocal && vicinityChatConfig.isEnabled() && vicinityChatConfig.getLocalChatConfig().isEnabledFor(player) ? Component.empty()
                .append(format(player, net.kyori.adventure.text.Component.empty(), vicinityChatConfig.getLocalChatConfig().getPrefix(),
                        false, true))
                .append(output) : output;
    }

    public static BooleanObjectPair<Component> filter(String text) {
        return filter(Component.literal(text));
    }

    public static BooleanObjectPair<Component> filter(Component component) {
        BooleanObjectPair<net.kyori.adventure.text.Component> filter = filter(VanillaComponentSerializer.vanilla().deserialize(component));
        return filter.leftBoolean() ? BooleanObjectPair.of(true, component) : BooleanObjectPair.of(false,
                VanillaComponentSerializer.vanilla().serialize(filter.right()));
    }

    public static BooleanObjectPair<net.kyori.adventure.text.Component> filter(net.kyori.adventure.text.Component component) {
        FilteringConfig filteringConfig = Config.getInstance().getFilteringConfig();
        if (!filteringConfig.isEnabled()) return BooleanObjectPair.of(true, component);

        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        if (!filteringConfig.getPattern().matcher(plain).matches())
            return BooleanObjectPair.of(false, component.replaceText(filteringConfig.getReplacementConfig()));

        return BooleanObjectPair.of(true, component);
    }

    public static MiniMessage createMiniMessage(TextReplacementConfig placeholderReplacement) {
        return MiniMessage.builder()
                .editTags(b -> b.tag("preprocess", (arg, ctx) -> Tag.preProcessParsed(
                        PlainTextComponentSerializer.plainText().serialize(net.kyori.adventure.text.Component.text(arg.popOr("No arg").value())
                                .replaceText(placeholderReplacement)))))
                .postProcessor(post -> post.replaceText(placeholderReplacement))
                .build();
    }

    public static void setFormattingMessageArgument(boolean formattingMessageArgument) {
        Chattix.formattingMessageArgument = formattingMessageArgument;
    }

    @SneakyThrows
    public static void setChatDisabled(boolean chatDisabled) {
        Chattix.chatDisabled = chatDisabled;

        if (chatDisabled && !Files.exists(chatDisabledFile)) Files.createFile(chatDisabledFile);
        else if (!chatDisabled) Files.deleteIfExists(chatDisabledFile);
    }

    @Nullable
    public static MutableComponent getErrorMsg(ServerPlayer player, String msg) {
        return predicates.stream()
                .map(predicate -> predicate.apply(player, msg))
                .filter(Objects::nonNull)
                .findFirst()
                .map(error -> error.withStyle(ChatFormatting.RED))
                .orElse(null);
    }

    public static String getLastMessage(Player player) {
        return lastMessages.getOrDefault(player.getUUID(), "");
    }

    public static void setLastMessages(Player player, String message) {
        lastMessages.put(player.getUUID(), message);
    }
}
