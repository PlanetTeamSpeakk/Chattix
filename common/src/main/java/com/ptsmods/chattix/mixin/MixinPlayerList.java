package com.ptsmods.chattix.mixin;

import com.ptsmods.chattix.config.Config;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class MixinPlayerList {

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
