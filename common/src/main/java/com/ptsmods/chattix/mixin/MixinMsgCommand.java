package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.commands.ReplyCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(MsgCommand.class)
public class MixinMsgCommand {
    @Inject(at = @At("HEAD"), method = "sendMessage")
    private static void sendMessage(CommandSourceStack stack, Collection<ServerPlayer> recipients, MessageArgument.ChatMessage chatMessage, CallbackInfoReturnable<Integer> cbi) {
        ReplyCommand.setLastRecipients(stack, recipients);
    }
}
