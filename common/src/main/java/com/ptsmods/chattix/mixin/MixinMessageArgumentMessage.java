package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.Chattix;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MessageArgument.Message.class)
public class MixinMessageArgumentMessage {

    @Inject(at = @At("HEAD"), method = "resolveDecoratedComponent")
    private void resolveDecoratedComponent_pre(CommandSourceStack stack, CallbackInfoReturnable<CompletableFuture<Component>> cbi) {
        Chattix.setFormattingMessageArgument(true);
    }

    @Inject(at = @At("RETURN"), method = "resolveDecoratedComponent")
    private void resolveDecoratedComponent_post(CommandSourceStack stack, CallbackInfoReturnable<CompletableFuture<Component>> cbi) {
        Chattix.setFormattingMessageArgument(false);
    }
}
