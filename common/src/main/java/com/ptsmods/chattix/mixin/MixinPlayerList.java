package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.util.ChattixArch;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    // Handle mutes
    @Inject(at = @At("HEAD"), method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;" +
            "Lnet/minecraft/network/chat/ChatSender;Lnet/minecraft/network/chat/ChatType$Bound;)V", cancellable = true)
    private void broadcastChatMessage(PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, ServerPlayer player, ChatSender chatSender, ChatType.Bound bound, CallbackInfo cbi) {
        //noinspection ConstantConditions // Not true, once again.
        if (Chattix.isChatDisabled() && !ChattixArch.hasPermission(player, "chattix.bypass")) {
            player.sendSystemMessage(Component.literal("Chat is currently disabled!")
                    .withStyle(ChatFormatting.RED));
            cbi.cancel();
            return;
        }

        if (player == null || !Config.getInstance().isMuted(player)) return;

        player.sendSystemMessage(Component.literal("You cannot speak as you are muted! Reason: ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                .append(Config.getInstance().getMuteReason(player)));

        Objects.requireNonNull(player.getServer()).sendSystemMessage(Component.literal("Player " + player.getGameProfile().getName() +
                " tried to speak but is muted!").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        cbi.cancel();
    }

    // Filter recipients to send the chat message to and handle chat mentions
    @Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/players/PlayerList;players:Ljava/util/List;"),
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatSender;Lnet/minecraft/network/chat/ChatType$Bound;)V")
    private List<ServerPlayer> broadcastChatMessage_players(PlayerList playerList, PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, @Nullable ServerPlayer player, ChatSender chatSender, ChatType.Bound bound) {
        String plain = playerChatMessage.signedContent().plain().toLowerCase(Locale.ROOT);
        if (player != null) Objects.requireNonNull(player.getServer()).getPlayerList().getPlayers().stream()
                .filter(Config.getInstance().getMentionsConfig()::isEnabledFor)
                .filter(p -> plain.contains(p.getGameProfile().getName().toLowerCase(Locale.ROOT)))
                .forEach(p -> p.playNotifySound(Objects.requireNonNull(Registry.SOUND_EVENT.get(Config.getInstance().getMentionsConfig().getSound())),
                        SoundSource.PLAYERS, 0.5f, 1f));

        return player == null ? playerList.getPlayers() : Config.getInstance().getVicinityChatConfig().filterRecipients(player).stream()
                .filter(recipient -> Config.getInstance().hasNotIgnored(recipient, player))
                .toList();
    }
}
