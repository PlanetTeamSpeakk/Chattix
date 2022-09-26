package com.ptsmods.chattix;

import com.google.common.cache.LoadingCache;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.config.FormattingConfig;
import com.ptsmods.chattix.config.MentionsConfig;
import com.ptsmods.chattix.config.VicinityChatConfig;
import com.ptsmods.chattix.placeholder.Placeholders;
import com.ptsmods.chattix.util.VanillaComponentSerializer;
import dev.architectury.event.events.common.ChatEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Chattix {
    public static final String MOD_ID = "chattix";
    public static final Logger LOG = LogManager.getLogger();
    private static boolean formattingMessageArgument = false;

    public static void init() {
        Config.load();

        // Workaround for a bug (I think) in Forge.
        // Currently, whenever you try to load classes in an event (or perhaps specifically async ones), the wrong
        // classloader is used. Instead of the classloader that loaded this mod file, the built-in system classloader is
        // used which does not know about this mod file nor its contents and thus cannot find its classes.
        // To work around this, we load all necessary classes by formatting an empty message beforehand.
        format(null, net.kyori.adventure.text.Component.empty());
        Placeholders.init(); // We call this for the same reason.

        ChatEvent.DECORATE.register((player, component) -> {
            if (!formattingMessageArgument && player != null && Config.getInstance().getFormattingConfig().isEnabled())
                component.set(format(player, VanillaComponentSerializer.vanilla().deserialize(component.get())));
        });

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            registerChattixCommand(dispatcher);
            registerIgnoreCommand(dispatcher);
            registerMentionsCommand(dispatcher);
        });

        PlayerEvent.PLAYER_JOIN.register(player -> Config.getInstance().getIgnored().refresh(player.getUUID()));
        PlayerEvent.PLAYER_QUIT.register(player -> Config.getInstance().getIgnored().invalidate(player.getUUID()));
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message) {
        return format(player, message, Config.getInstance().getFormattingConfig().getActiveFormatFor(player));
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message, String format) {
        TextReplacementConfig placeholderReplacement = Placeholders.createReplacementConfig(player, message);
        MiniMessage miniMessage = MiniMessage.builder()
                .tags(TagResolver.standard())
                .postProcessor(post -> post.replaceText(placeholderReplacement))
                .build();

        return VanillaComponentSerializer.vanilla().serialize(miniMessage.deserialize(format));
    }

    public static void setFormattingMessageArgument(boolean formattingMessageArgument) {
        Chattix.formattingMessageArgument = formattingMessageArgument;
    }

    private static void registerChattixCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("chattix")
                .requires(stack -> stack.hasPermission(2))
                .then(literal("reload")
                        .executes(ctx -> {
                            long start = System.currentTimeMillis();
                            boolean wasFormattingEnabled = Config.getInstance().getFormattingConfig().isEnabled();
                            Config.load();

                            LoadingCache<UUID, Set<UUID>> ignored = Config.getInstance().getIgnored();
                            ctx.getSource().getServer().getPlayerList().getPlayers().forEach(player -> ignored.refresh(player.getUUID()));

                            if (Config.getInstance().getFormattingConfig().isEnabled() != wasFormattingEnabled)
                                ctx.getSource().sendFailure(Component.literal("Chat formatting was toggled. " +
                                        "All players should relog asap."));

                            ctx.getSource().sendSuccess(Component.literal("Reloaded the Chattix config in " +
                                    (System.currentTimeMillis() - start) + " ms."), true);
                            return 1;
                        }))
                .then(literal("debug")
                        .executes(ctx -> executeDebug(ctx, ctx.getSource().getPlayerOrException(), null))
                        .then(argument("target", EntityArgument.player())
                                .executes(ctx -> executeDebug(ctx, EntityArgument.getPlayer(ctx, "target"), null))
                                .then(argument("format", StringArgumentType.greedyString())
                                        .executes(ctx -> executeDebug(ctx, EntityArgument.getPlayer(ctx, "target"), ctx.getArgument("format", String.class))))))
                .then(literal("docs")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(Component.literal("Click here to go to the documentation site.")
                                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://chattix.ptsmods.com"))), false);
                            return 1;
                        })));
    }

    private static int executeDebug(CommandContext<CommandSourceStack> ctx, ServerPlayer target, String format) {
        CommandSourceStack source = ctx.getSource();
        FormattingConfig formattingConfig = Config.getInstance().getFormattingConfig();
        VicinityChatConfig vicinityChatConfig = Config.getInstance().getVicinityChatConfig();
        format = format == null ? formattingConfig.getActiveFormatFor(target) : format;

        source.sendSuccess(Component.literal("Chattix debug information for ").append(target.getDisplayName()).append(Component.literal(":")), true);
        source.sendSuccess(Component.literal("Group: " + formattingConfig.getGroupsFor(target).stream()
                .findFirst()
                .map(FormattingConfig.GroupConfig.Group::getName)
                .orElse("none")), false);
        source.sendSuccess(Component.literal("Format: " + format), false);
        source.sendSuccess(Component.literal("Formatted test msg: ").append(format(target, net.kyori.adventure.text.Component.text("Hello world!"), format)), false);
        source.sendSuccess(Component.literal("Current recipients: " + (vicinityChatConfig.isEnabled() ? vicinityChatConfig.filterRecipients(target).stream()
                .map(player -> player.getGameProfile().getName())
                .collect(Collectors.joining(", ")) : "everyone")), false);

        return 1;
    }

    private static void registerIgnoreCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("ignore")
                .executes(ctx -> {
                    Set<UUID> ignored = Config.getInstance().getIgnored().getIfPresent(ctx.getSource().getPlayerOrException().getUUID());
                    if (ignored == null || ignored.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("You're not ignoring anyone."));
                        return 0;
                    }

                    ctx.getSource().sendSuccess(Component.literal("You're currently ignoring the following players: " + ignored.stream()
                            .map(id -> Optional.ofNullable(ctx.getSource().getServer().getPlayerList().getPlayer(id))
                                    .map(player -> player.getGameProfile().getName())
                                    .orElseGet(id::toString))
                            .collect(Collectors.joining(", "))), false);

                    return ignored.size();
                })
                .then(argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            if (player == ctx.getSource().getPlayerOrException()) {
                                ctx.getSource().sendFailure(Component.literal("You cannot ignore yourself."));
                                return 0;
                            }

                            Set<UUID> ignored = Config.getInstance().getIgnored().getUnchecked(ctx.getSource().getPlayerOrException().getUUID());

                            if (ignored.contains(player.getUUID())) {
                                ignored.remove(player.getUUID());
                                ctx.getSource().sendSuccess(Component.empty()
                                        .append(Component.literal("You're no longer ignoring "))
                                        .append(player.getDisplayName())
                                        .append(Component.literal(".")), false);
                            } else {
                                ignored.add(player.getUUID());
                                ctx.getSource().sendSuccess(Component.empty()
                                        .append(Component.literal("You're now ignoring "))
                                        .append(player.getDisplayName())
                                        .append(Component.literal(".")), false);
                            }

                            Config.getInstance().storeIgnoredFor(ctx.getSource().getPlayerOrException());
                            return ignored.contains(player.getUUID()) ? 2 : 1;
                        })));
    }

    private static void registerMentionsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("mentions")
                .executes(ctx -> {
                    MentionsConfig mentionsConfig = Config.getInstance().getMentionsConfig();
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    mentionsConfig.toggleFor(player);

                    ctx.getSource().sendSuccess(Component.literal("Chat mentions are now " +
                            (mentionsConfig.isEnabledFor(player) ? "enabled" : "disabled") + "."), true);

                    return mentionsConfig.isEnabledFor(player) ? 2 : 1;
                }));
    }
}
