package com.ptsmods.chattix.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.ptsmods.chattix.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MessageArgument.class)
public class MixinMessageArgument {
    private static final @Unique DynamicCommandExceptionType MUTED = new DynamicCommandExceptionType(p -> Component.literal("You cannot speak as you are muted! Reason: ")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
            .append(Config.getInstance().getMuteReason((Player) p)));

    @Inject(at = @At("HEAD"), method = "getMessage")
    private static void getMessage(CommandContext<CommandSourceStack> ctx, String string, CallbackInfoReturnable<Component> cbi) throws CommandSyntaxException {
        checkMuted(ctx);
    }

    @Inject(at = @At("HEAD"), method = "getChatMessage")
    private static void getChatMessage(CommandContext<CommandSourceStack> ctx, String string, CallbackInfoReturnable<Component> cbi) throws CommandSyntaxException {
        checkMuted(ctx);
    }

    private static @Unique void checkMuted(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if (ctx.getSource().isPlayer() && Config.getInstance().isMuted(ctx.getSource().getPlayerOrException()))
            throw MUTED.create(ctx.getSource().getPlayerOrException());
    }
}
