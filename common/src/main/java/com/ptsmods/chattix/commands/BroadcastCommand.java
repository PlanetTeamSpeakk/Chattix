package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.placeholder.Placeholders;
import com.ptsmods.chattix.util.ChattixArch;
import com.ptsmods.chattix.util.VanillaComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BroadcastCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ChattixArch.registerPermission("chattix.broadcast", false);
        dispatcher.register(literal("broadcast")
                .requires(stack -> ChattixArch.hasPermission(stack, "chattix.broadcast", false))
                .then(argument("message", MessageArgument.message())
                        .executes(ctx -> {
                            net.minecraft.network.chat.Component message = MessageArgument.getMessage(ctx, "message");
                            Component adventureMessage = VanillaComponentSerializer.vanilla().deserialize(message);
                            ServerPlayer sender = ctx.getSource().isPlayer() ? ctx.getSource().getPlayerOrException() : null;

                            ctx.getSource().getServer().getPlayerList().broadcastSystemMessage(message, player -> {
                                PlaceholderContext placeholderContext = new PlaceholderContext(PlaceholderContext.ContextType.BROADCAST, sender, player);

                                // Parse message separately for every player in case of placeholders.
                                return VanillaComponentSerializer.vanilla().serialize(Chattix.createMiniMessage(placeholderContext, player, Component.empty())
                                        .deserialize(LegacyComponentSerializer.legacySection().serialize(adventureMessage.replaceText(
                                                Placeholders.createReplacementConfig(placeholderContext, player, Component.empty())))));
                            }, false);

                            return 1;
                        })));
    }
}
