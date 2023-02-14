package com.ptsmods.chattix.mixin;

import com.google.common.collect.ImmutableList;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.config.ModerationConfig;
import com.ptsmods.chattix.util.ChattixArch;
import com.ptsmods.chattix.util.addons.ServerPlayerAddon;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Shadow @Final private MinecraftServer server;
    @SuppressWarnings("ConstantConditions")
    private static final @Unique List<BiFunction<ServerPlayer, PlayerChatMessage, MutableComponent>> predicates = ImmutableList.of(
            (player, msg) -> Chattix.isChatDisabled() && !ChattixArch.hasPermission(player, "chattix.bypass", false) ?
                    Component.literal("Chat is currently disabled!") : null,
            (player, msg) -> {
                BooleanObjectPair<Component> filter;
                if (ChattixArch.hasPermission(player, "chattix.bypass", false) ||
                        (filter = Chattix.filter(msg.signedContent())).leftBoolean()) return null;

                return Component.literal("That message contains illegal characters! Problematic characters: ").append(filter.right());
            },
            (player, msg) -> {
                if (!Config.getInstance().isMuted(player)) return null;

                Objects.requireNonNull(player.getServer()).sendSystemMessage(Component.literal("Player " + player.getGameProfile().getName() +
                        " tried to speak but is muted!").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

                return Component.literal("You cannot speak as you are muted! Reason: ")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                        .append(Config.getInstance().getMuteReason(player));
            },
            (player, msg) -> {
                if (ChattixArch.hasPermission(player, "chattix.bypass", false)) return null;

                ModerationConfig.SlowModeConfig slowModeConfig = Config.getInstance().getModerationConfig().getSlowModeConfig();
                int remaining = (int) (slowModeConfig.getCooldown() - (System.currentTimeMillis() - slowModeConfig.getLastSent(player)) / 1000);
                return slowModeConfig.isOnCooldown(player) ?
                        Component.literal("Too fast! Slow mode is enabled and you need to wait " +
                                remaining + " more second" + (remaining == 1 ? "" : "s") + ".") : null;
            },
            (player, msg) -> !ChattixArch.hasPermission(player, "chattix.bypass", false) &&
                    Config.getInstance().getModerationConfig().isTooSimilar(msg.signedContent(), Chattix.getLastMessage(player)) ?
                    Component.literal("That message is too similar to your last message!") : null
    );

    @Inject(at = @At("HEAD"), method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;" +
            "Lnet/minecraft/network/chat/ChatType$Bound;)V", cancellable = true)
    private void broadcastChatMessage(PlayerChatMessage msg, Predicate<ServerPlayer> predicate, ServerPlayer player, ChatType.Bound bound, CallbackInfo cbi) {
        predicates.stream()
                .map(p -> p.apply(player, msg))
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresentOrElse(errorMsg -> {
                    player.sendSystemMessage(errorMsg.withStyle(ChatFormatting.RED));
                    cbi.cancel();
                }, () -> {
                    Config.getInstance().getModerationConfig().getSlowModeConfig().setLastSent(player);
                    Chattix.setLastMessages(player, msg.signedContent());
                });
    }

    // Filter recipients to send the chat message to and handle chat mentions
    @Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/players/PlayerList;players:Ljava/util/List;"),
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V")
    private List<ServerPlayer> broadcastChatMessage_players(PlayerList playerList, PlayerChatMessage playerChatMessage, Predicate<ServerPlayer> predicate, @Nullable ServerPlayer player, ChatType.Bound bound) {
        String plain = playerChatMessage.signedContent().toLowerCase(Locale.ROOT);
        if (player != null) Objects.requireNonNull(player.getServer()).getPlayerList().getPlayers().stream()
                .filter(Config.getInstance().getMentionsConfig()::isEnabledFor)
                .filter(p -> plain.contains(p.getGameProfile().getName().toLowerCase(Locale.ROOT)))
                .forEach(p -> p.playNotifySound(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(Config.getInstance().getMentionsConfig().getSound())),
                        SoundSource.PLAYERS, 0.5f, 1f));

        return player == null ? playerList.getPlayers() : Config.getInstance().getVicinityChatConfig().filterRecipients(player).stream()
                .filter(recipient -> Config.getInstance().hasNotIgnored(recipient, player))
                .toList();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"), method = "placeNewPlayer")
    private MutableComponent placeNewPlayer_translatable(String key, Object[] args, Connection connection, ServerPlayer player) {
        return (MutableComponent) switch (key) {
            case "multiplayer.player.joined" -> Chattix.format(player, net.kyori.adventure.text.Component.empty(), Config.getInstance().getJoinLeaveConfig().getJoinFormat(), false);
            case "multiplayer.player.joined.renamed" -> Chattix.format(player, net.kyori.adventure.text.Component.empty(), Config.getInstance().getJoinLeaveConfig().getJoinChangedNameFormat(), false);
            default -> Component.translatable(key, args);
        };
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), method = "placeNewPlayer")
    private void placeNewPlayer_broadcastSystemMessage(PlayerList instance, Component message, boolean bl) {
        if (Config.getInstance().getJoinLeaveConfig().isEnabled())
            instance.broadcastSystemMessage(message, bl);
    }

    @Inject(at = @At("HEAD"), method = "placeNewPlayer")
    private void placeNewPlayer(Connection connection, ServerPlayer player, CallbackInfo ci) {
        if (server.getProfileCache().get(player.getGameProfile().getId()).isEmpty())
            ((ServerPlayerAddon) player).setFirstTimePlaying(true);
    }
}
