package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.mixin.MixinMsgCommandAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ReplyCommand {
    private static final Map<UUID, Collection<ServerPlayer>> lastRecipients = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("r").redirect(dispatcher.register(literal("reply")
                .then(argument("msg", MessageArgument.message())
                        .executes(ctx -> {
                            if (!lastRecipients.containsKey(ctx.getSource().getPlayerOrException().getUUID())) {
                                ctx.getSource().sendFailure(Component.literal("You have not received nor sent any direct messages."));
                                return 0;
                            }

                            MessageArgument.ChatMessage msg = MessageArgument.getChatMessage(ctx, "msg");
                            Collection<ServerPlayer> recipients = lastRecipients.get(ctx.getSource().getPlayerOrException().getUUID());
                            MixinMsgCommandAccessor.callSendMessage(ctx.getSource(), recipients, msg);

                            return recipients.size();
                        })))));
    }

    public static void setLastRecipients(CommandSourceStack stack, Collection<ServerPlayer> recipients) {
        if (!stack.isPlayer()) return;

        lastRecipients.put(Objects.requireNonNull(stack.getPlayer()).getUUID(), recipients);
        Set<ServerPlayer> singleton = Collections.singleton(stack.getPlayer());
        recipients.forEach(recipient -> lastRecipients.put(recipient.getUUID(), singleton));
    }
}
