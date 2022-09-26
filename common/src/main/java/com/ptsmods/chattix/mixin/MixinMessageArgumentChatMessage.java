package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.Chattix;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(MessageArgument.ChatMessage.class)
public abstract class MixinMessageArgumentChatMessage {

    @Inject(at = @At("HEAD"), method = "method_45069")
    private void resolve_pre(CommandSourceStack commandSourceStack, MinecraftServer minecraftServer, Consumer<?> consumer, CallbackInfoReturnable<CompletableFuture<Void>> cbi) {
        Chattix.setFormattingMessageArgument(true);
    }

    @Inject(at = @At("RETURN"), method = "method_45069")
    private void resolve_post(CommandSourceStack commandSourceStack, MinecraftServer minecraftServer, Consumer<?> consumer, CallbackInfoReturnable<CompletableFuture<Void>> cbi) {
        Chattix.setFormattingMessageArgument(false);
    }
}
