package com.ptsmods.chattix.util;

import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class MixinHelper {
    /**
     * Serves as an override for the decorator used when
     * resolving message arguments.
     */
    @Getter
    private static ChatDecorator decoratorOverride;

    public static void setDecoratorOverride(ChatDecorator decorator) {
        decoratorOverride = decorator;
    }

    public static void setDecoratorOverride(ServerPlayer target) {
        setDecoratorOverride(Config.getInstance().getFormattingConfig().isEnabled() ? (player, message) ->
                CompletableFuture.supplyAsync(() -> Chattix.format(player, VanillaComponentSerializer.vanilla().deserialize(message),
                        Config.getInstance().getFormattingConfig().getMsgFormat(), new PlaceholderContext(PlaceholderContext.ContextType.MSG,
                                player, target), true, true)) : null);
    }
}
