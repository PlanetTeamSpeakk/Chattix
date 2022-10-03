package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.config.VicinityChatConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class LocalCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("local")
                .executes(ctx -> {
                    if (!Config.getInstance().getVicinityChatConfig().isEnabled()) {
                        ctx.getSource().sendFailure(Component.literal("Vicinity chat is not enabled on this server!"));
                        return 0;
                    }

                    VicinityChatConfig.LocalChatConfig localChatConfig = Config.getInstance().getVicinityChatConfig().getLocalChatConfig();
                    localChatConfig.toggleFor(ctx.getSource().getPlayerOrException());
                    ctx.getSource().sendSuccess(Component.literal("Local chat has been " +
                            (localChatConfig.isEnabledFor(ctx.getSource().getPlayerOrException()) ? "enabled" : "disabled") + "."), true);

                    return localChatConfig.isEnabledFor(ctx.getSource().getPlayerOrException()) ? 2 : 1;
                }));
    }
}
