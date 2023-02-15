package com.ptsmods.chattix.placeholder.placeholders;

import com.ptsmods.chattix.placeholder.Placeholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class MessagePlaceholder implements Placeholder {
    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return escapeMMTags(message);
    }

    /**
     * Escapes all minimessage tags in a component.
     * @param component
     * @return
     */
    private Component escapeMMTags(Component component) {
        Component newComp = component instanceof TextComponent text ?
                Component.text(MiniMessage.miniMessage().escapeTags(text.content()))
                        .style(component.style()) : component;
        return newComp.children(component.children().stream()
                .map(this::escapeMMTags)
                .toList());
    }
}
