package com.ptsmods.chattix.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.util.ChattixArch;
import com.ptsmods.chattix.util.MixinHelper;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

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

    @Inject(at = @At("HEAD"), method = "resolveChatMessage")
    private static void resolveChatMessagePre(CommandContext<CommandSourceStack> ctx, String string, Consumer<PlayerChatMessage> consumer, CallbackInfo cbi) throws CommandSyntaxException {
        Chattix.setFormattingMessageArgument(true);

        if (ctx.getSource().isPlayer() && Config.getInstance().isMuted(ctx.getSource().getPlayerOrException()))
            throw MUTED.create(ctx.getSource().getPlayerOrException());

        //noinspection ConstantConditions // Not true
        if (Chattix.isChatDisabled() && ctx.getSource().isPlayer() &&
                !ChattixArch.hasPermission(ctx.getSource().getPlayerOrException(), "chattix.bypass", false))
            throw DISABLED.create();
    }

    @Inject(at = @At("TAIL"), method = "resolveChatMessage", locals = LocalCapture.CAPTURE_FAILHARD)
    private static void resolveChatMessagePost(CommandContext<CommandSourceStack> ctx, String string,
                                               Consumer<PlayerChatMessage> consumer, CallbackInfo cbi,
                                               MessageArgument.Message msg, CommandSourceStack source,
                                               Component comp) throws CommandSyntaxException {
        Chattix.setFormattingMessageArgument(false);

        //noinspection ConstantConditions // Not true
        if (!ctx.getSource().isPlayer() || ChattixArch.hasPermission(ctx.getSource().getPlayerOrException(),
                "chattix.bypass", false)) return;

        BooleanObjectPair<Component> filter = Chattix.filter(comp);
        if (!filter.leftBoolean())
            throw ILLEGAL_CHARACTERS.create(filter.right());
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getChatDecorator()Lnet/minecraft/network/chat/ChatDecorator;"),
            method = {"resolveSignedMessage", "resolveDisguisedMessage"})
    private static ChatDecorator getChatDecorator(MinecraftServer server) {
        return MixinHelper.getDecoratorOverride() == null ? server.getChatDecorator() : MixinHelper.getDecoratorOverride();
    }
}
