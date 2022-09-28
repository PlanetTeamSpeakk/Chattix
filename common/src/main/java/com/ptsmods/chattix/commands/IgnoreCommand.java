package com.ptsmods.chattix.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.ptsmods.chattix.config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class IgnoreCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("ignore")
                .executes(ctx -> {
                    Set<UUID> ignored = Config.getInstance().getIgnored().getIfPresent(ctx.getSource().getPlayerOrException().getUUID());
                    if (ignored == null || ignored.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("You're not ignoring anyone."));
                        return 0;
                    }

                    GameProfileCache profileCache = ctx.getSource().getServer().getProfileCache();
                    ctx.getSource().sendSuccess(Component.literal("You're currently ignoring the following players: " + ignored.stream()
                            .map(id -> Optional.ofNullable(ctx.getSource().getServer().getPlayerList().getPlayer(id))
                                    .map(player -> player.getGameProfile().getName())
                                    .or(() -> profileCache.get(id).map(GameProfile::getName))
                                    .orElseGet(id::toString))
                            .collect(Collectors.joining(", "))), false);

                    return ignored.size();
                })
                .then(argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            if (player == ctx.getSource().getPlayerOrException()) {
                                ctx.getSource().sendFailure(Component.literal("You cannot ignore yourself."));
                                return 0;
                            }

                            Set<UUID> ignored = Config.getInstance().getIgnored().getUnchecked(ctx.getSource().getPlayerOrException().getUUID());

                            if (ignored.contains(player.getUUID())) {
                                ignored.remove(player.getUUID());
                                ctx.getSource().sendSuccess(Component.empty()
                                        .append(Component.literal("You're no longer ignoring "))
                                        .append(player.getDisplayName())
                                        .append(Component.literal(".")), false);
                            } else {
                                ignored.add(player.getUUID());
                                ctx.getSource().sendSuccess(Component.empty()
                                        .append(Component.literal("You're now ignoring "))
                                        .append(player.getDisplayName())
                                        .append(Component.literal(".")), false);
                            }

                            Config.getInstance().storeIgnoredFor(ctx.getSource().getPlayerOrException());
                            return ignored.contains(player.getUUID()) ? 2 : 1;
                        })));
    }
}
