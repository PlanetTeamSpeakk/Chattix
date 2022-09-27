package com.ptsmods.chattix.commands;

import com.google.common.cache.LoadingCache;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.config.FormattingConfig;
import com.ptsmods.chattix.config.VicinityChatConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ChattixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
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

    public static int executeDebug(CommandContext<CommandSourceStack> ctx, ServerPlayer target, String format) {
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
        source.sendSuccess(Component.literal("Formatted test msg: ").append(Chattix.format(target, net.kyori.adventure.text.Component.text("Hello world!"), format)), false);
        source.sendSuccess(Component.literal("Current recipients: " + (vicinityChatConfig.isEnabled() ? vicinityChatConfig.filterRecipients(target).stream()
                .map(player -> player.getGameProfile().getName())
                .collect(Collectors.joining(", ")) : "everyone")), false);

        return 1;
    }
}
