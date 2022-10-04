package com.ptsmods.chattix.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(MsgCommand.class)
public interface MixinMsgCommandAccessor {
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    @Invoker
    static int callSendMessage(CommandSourceStack stack, Collection<ServerPlayer> recipients, MessageArgument.ChatMessage message) {
        throw new AssertionError("This shouldn't happen.");
    }
}
