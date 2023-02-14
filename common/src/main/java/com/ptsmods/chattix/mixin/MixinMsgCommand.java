package com.ptsmods.chattix.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ptsmods.chattix.commands.ReplyCommand;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.util.MixinHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Mixin(MsgCommand.class)
public class MixinMsgCommand {
    private static final @Unique ResourceKey<ChatType> formattedChatType = ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation("chattix:formatted"));

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/MessageArgument;resolveChatMessage(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;Ljava/util/function/Consumer;)V"), method = "*")
    private static void resolveChatMessage(CommandContext<CommandSourceStack> ctx, String arg, Consumer<PlayerChatMessage> consumer) throws CommandSyntaxException {
        List<ServerPlayer> targets = new ArrayList<>(EntityArgument.getPlayers(ctx, "targets"));

        MixinHelper.setDecoratorOverride(targets.isEmpty() ? null : targets.get(0));

        MessageArgument.resolveChatMessage(ctx, arg, consumer);
        MixinHelper.setDecoratorOverride((ChatDecorator) null);
    }

    @Inject(at = @At("HEAD"), method = "sendMessage")
    private static void sendMessage(CommandSourceStack source, Collection<ServerPlayer> recipients, PlayerChatMessage playerChatMessage, CallbackInfo ci) {
        ReplyCommand.setLastRecipients(source, recipients);
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/network/chat/ChatType;MSG_COMMAND_INCOMING:Lnet/minecraft/resources/ResourceKey;"), method = "sendMessage")
    private static ResourceKey<ChatType> sendMessage_incoming() {
        return Config.getInstance().getFormattingConfig().isEnabled() ? formattedChatType : ChatType.MSG_COMMAND_INCOMING;
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/network/chat/ChatType;MSG_COMMAND_OUTGOING:Lnet/minecraft/resources/ResourceKey;"), method = "sendMessage")
    private static ResourceKey<ChatType> sendMessage_outgoing() {
        return Config.getInstance().getFormattingConfig().isEnabled() ? formattedChatType : ChatType.MSG_COMMAND_OUTGOING;
    }
}
