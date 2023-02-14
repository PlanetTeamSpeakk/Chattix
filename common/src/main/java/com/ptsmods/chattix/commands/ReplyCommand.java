package com.ptsmods.chattix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.mixin.MixinMsgCommandAccessor;
import com.ptsmods.chattix.util.MixinHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ReplyCommand {
    private static final Map<UUID, Collection<ServerPlayer>> lastRecipients = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("r").redirect(dispatcher.register(literal("reply")
                .then(argument("message", MessageArgument.message())
                        .executes(ctx -> {
                            if (!lastRecipients.containsKey(ctx.getSource().getPlayerOrException().getUUID())) {
                                ctx.getSource().sendFailure(Component.literal("You have not received nor sent any direct messages."));
                                return 0;
                            }

                            Collection<ServerPlayer> recipients = lastRecipients.get(ctx.getSource().getPlayerOrException().getUUID());
                            MixinHelper.setDecoratorOverride(recipients.isEmpty() ? null : recipients.iterator().next());

                            MessageArgument.resolveChatMessage(ctx, "message", msg ->
                                    MixinMsgCommandAccessor.callSendMessage(ctx.getSource(), recipients, msg));

                            MixinHelper.setDecoratorOverride((ChatDecorator) null);

                            return recipients.size();
                        })))));
    }

    public static void setLastRecipients(CommandSourceStack stack, Collection<ServerPlayer> recipients) {
        if (!stack.isPlayer()) return;

        // Ensure we only maintain weak references to players. When players log off, we don't want to keep them here.
        Set<ServerPlayer> weakSender = Collections.newSetFromMap(new WeakHashMap<>());
        weakSender.add(stack.getPlayer());
        recipients.forEach(recipient -> lastRecipients.put(recipient.getUUID(), weakSender));

        Set<ServerPlayer> weakRecipients = Collections.newSetFromMap(new WeakHashMap<>());
        weakRecipients.addAll(recipients);
        lastRecipients.put(Objects.requireNonNull(stack.getPlayer()).getUUID(), weakRecipients);
    }
}
