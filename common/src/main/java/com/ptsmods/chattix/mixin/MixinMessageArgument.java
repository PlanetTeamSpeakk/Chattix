package com.ptsmods.chattix.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.util.ChattixArch;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
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
    private static final @Unique DynamicCommandExceptionType MUTED = new DynamicCommandExceptionType(p -> Component.literal(
            "You cannot speak as you are muted! Reason: ")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
            .append(Config.getInstance().getMuteReason((Player) p)));
    private static final @Unique SimpleCommandExceptionType DISABLED = new SimpleCommandExceptionType(Component.literal(
            "Chat is currently disabled!"));
    private static final @Unique DynamicCommandExceptionType ILLEGAL_CHARACTERS = new DynamicCommandExceptionType(o -> Component.literal(
            "That message contains illegal characters! Problematic characters: ").append((Component) o));

    @Inject(at = @At("HEAD"), method = "getChatMessage")
    private static void getChatMessage(CommandContext<CommandSourceStack> ctx, String string, CallbackInfoReturnable<Component> cbi) throws CommandSyntaxException {
        if (ctx.getSource().isPlayer() && Config.getInstance().isMuted(ctx.getSource().getPlayerOrException()))
            throw MUTED.create(ctx.getSource().getPlayerOrException());

        //noinspection ConstantConditions // Not true
        if (Chattix.isChatDisabled() && !ctx.getSource().isPlayer() &&
                !ChattixArch.hasPermission(ctx.getSource().getPlayerOrException(), "chattix.bypass", false))
            throw DISABLED.create();
    }

    @Inject(at = @At("RETURN"), method = "getChatMessage")
    private static void getChatMessageReturn(CommandContext<CommandSourceStack> ctx, String string, CallbackInfoReturnable<MessageArgument.ChatMessage> cbi) throws CommandSyntaxException {
        //noinspection ConstantConditions // Not true
        if (!ctx.getSource().isPlayer() || ChattixArch.hasPermission(ctx.getSource().getPlayerOrException(), "chattix.bypass", false)) return;

        BooleanObjectPair<Component> filter = Chattix.filter(cbi.getReturnValue().signedArgument().signedContent().decorated());
        if (!filter.leftBoolean())
            throw ILLEGAL_CHARACTERS.create(filter.right());
    }
}
