package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.util.ChattixArch;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class DisableChatCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ChattixArch.registerPermission("chattix.disablechat", false);

        dispatcher.register(literal("disablechat")
                .requires(stack -> ChattixArch.hasPermission(stack, "chattix.disablechat"))
                .executes(ctx -> {
                    Chattix.setChatDisabled(!Chattix.isChatDisabled());
                    ctx.getSource().sendSuccess(Component.literal("Chat has been " +
                            (Chattix.isChatDisabled() ? "disabled" : "enabled") + "."), true);

                    return Chattix.isChatDisabled() ? 1 : 2;
                }));
    }
}
