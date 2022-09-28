package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.Config;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
    @Shadow public ServerPlayer player;
    private static final @Unique ResourceKey<ChatType> formattedChatType = ResourceKey.create(Registry.CHAT_TYPE_REGISTRY, new ResourceLocation("chattix:formatted"));

    @Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/network/chat/ChatType;CHAT:Lnet/minecraft/resources/ResourceKey;"), method = "broadcastChatMessage")
    private ResourceKey<ChatType> broadcastChatMessage_chat() {
        return Config.getInstance().getFormattingConfig().isEnabled() ? formattedChatType : ChatType.CHAT;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), method = "onDisconnect")
    private void onDisconnect_broadcastSystemMessage(PlayerList instance, Component component, boolean bl) {
        if (Config.getInstance().getJoinLeaveConfig().isEnabled())
            instance.broadcastSystemMessage(Chattix.format(player, net.kyori.adventure.text.Component.empty(),
                    Config.getInstance().getJoinLeaveConfig().getLeaveFormat()), bl);
    }
}
