package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.util.ChattixArch;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import static net.minecraft.commands.Commands.literal;

public class ChatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ChattixArch.registerPermission("chattix.managechat", false);
        dispatcher.register(literal("chat")
                .requires(stack -> ChattixArch.hasPermission(stack, "chattix.managechat"))
                .then(literal("clear").executes(ChatCommand::executeClear))
                .then(literal("disable").executes(ChatCommand::executeDisable)));
    }

    private static int executeClear(CommandContext<CommandSourceStack> ctx) {
        MutableComponent clearComponent = Component.empty();
        for (int i = 0; i < 100; i++)
            clearComponent.append(Component.literal("\n"));

        ctx.getSource().getServer().getPlayerList().getPlayers().stream()
                .filter(player -> !ChattixArch.hasPermission(player, "chattix.bypass"))
                .forEach(player -> player.sendSystemMessage(clearComponent));
        ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(Component.literal(
                        "Chat has been cleared by a moderator.")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                        .withBold(true)), false);
        ctx.getSource().sendSuccess(Component.literal("Chat has been cleared for everyone without the " +
                "chattix.bypass permission."), true);

        return 1;
    }

    private static int executeDisable(CommandContext<CommandSourceStack> ctx) {
        Chattix.setChatDisabled(!Chattix.isChatDisabled());
        ctx.getSource().sendSuccess(Component.literal("Chat has been " +
                (Chattix.isChatDisabled() ? "disabled" : "enabled") + "."), true);

        return Chattix.isChatDisabled() ? 1 : 2;
    }
}
