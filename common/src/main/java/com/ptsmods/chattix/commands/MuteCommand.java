package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.util.ChattixArch;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MuteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ChattixArch.registerPermission("chattix.mute", false);

        dispatcher.register(literal("mute")
                .requires(stack -> ChattixArch.hasPermission(stack, "chattix.mute", false))
                .then(argument("player", EntityArgument.player())
                        .executes(ctx -> executeMute(ctx, Component.literal("No reason specified")))
                        .then(argument("reason", MessageArgument.message())
                                .executes(ctx -> executeMute(ctx, MessageArgument.getMessage(ctx, "reason"))))));

        dispatcher.register(literal("unmute")
                .requires(stack -> ChattixArch.hasPermission(stack, "chattix.mute", false))
                .then(argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            if (!Config.getInstance().isMuted(player)) {
                                ctx.getSource().sendFailure(Component.literal("That player is not muted."));
                                return 0;
                            }

                            Config.getInstance().unmute(player);
                            player.sendSystemMessage(Component.literal("You have been unmuted!").withStyle(ChatFormatting.GREEN));
                            ctx.getSource().sendSuccess(Component.literal("Player " + player.getGameProfile().getName() +
                                    " has been unmuted."), true);

                            return 1;
                        })));
    }

    private static int executeMute(CommandContext<CommandSourceStack> ctx, Component reason) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        Config.getInstance().mute(player, reason);

        player.sendSystemMessage(Component.literal("You have been muted! Reason: ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                .append(reason));
        ctx.getSource().sendSuccess(Component.literal("Player " + player.getGameProfile().getName() + " has been muted!"), true);

        return 1;
    }
}
