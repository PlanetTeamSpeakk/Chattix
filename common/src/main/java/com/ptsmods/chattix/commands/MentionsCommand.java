package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.config.MentionsConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class MentionsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
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
