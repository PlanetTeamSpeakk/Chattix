package com.ptsmods.chattix.placeholder.placeholders;

import com.ptsmods.chattix.placeholder.ComponentPlaceholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class MessagePlaceholder implements ComponentPlaceholder {
    private static final MiniMessage miniMessage = MiniMessage.builder()
            .editTags(b -> b.tag("preprocess", (arg, ctx) -> Tag.preProcessParsed(arg.popOr("No arg").value())))
            .build();

    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        return escapeMMTags(message);
    }

    /**
     * Escapes all minimessage tags in a component.
     * @param component The component to escape tags in
     * @return A version of the given component with escaped tags.
     */
    private Component escapeMMTags(Component component) {
        // Using our own MiniMessage instance so preprocess tags are also escaped.
        Component newComp = component instanceof TextComponent text ?
                Component.text(miniMessage.escapeTags(text.content()))
                        .style(component.style()) : component;

        return newComp.children(component.children().stream()
                .map(this::escapeMMTags)
                .toList());
    }
}
