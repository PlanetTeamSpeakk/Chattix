package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.util.ChattixArch;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.literal;

public class SlowModeCommand {
    private static boolean enabled = false;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ChattixArch.registerPermission("chattix.slowmode", false);

        dispatcher.register(literal("slowmode")
                .requires(source -> ChattixArch.hasPermission(source, "chattix.slowmode", false))
                .executes(ctx -> {
                    enabled = !enabled;
                    boolean isEnabled = isEnabled();

                    ctx.getSource().sendSuccess(Component.literal("Slow-mode has been " + (isEnabled ? "enabled" : "disabled") + "."), true);
                    return isEnabled ? 2 : 1;
                })); // TODO advancement and death message formatting, message formatting and markdown
    }

    public static boolean isEnabled() {
        return Config.getInstance().getModerationConfig().getSlowModeConfig().isEnabled() ^ enabled;
    }
}
