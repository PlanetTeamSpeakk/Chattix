package com.ptsmods.chattix.placeholder.placeholders;

import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.config.FormattingConfig;
import com.ptsmods.chattix.placeholder.ComponentPlaceholder;
import com.ptsmods.chattix.placeholder.PlaceholderContext;
import com.ptsmods.chattix.util.ChattixArch;
import com.ptsmods.chattix.util.ComponentRenderer;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class MessagePlaceholder implements ComponentPlaceholder {
    private static final Parser mdParser = Parser.builder()
            .includeSourceSpans(IncludeSourceSpans.NONE)
            .extensions(List.of(StrikethroughExtension.create(), InsExtension.create()))
            .build();

    @Override
    public Component parse(@NonNull PlaceholderContext context, @NonNull ServerPlayer player, @NonNull Component message, @Nullable String arg) {
        FormattingConfig formattingConfig = Config.getInstance().getFormattingConfig();
        FormattingConfig.MarkdownConfig markdownConfig = formattingConfig.getMarkdownConfig();
        if (!formattingConfig.isEnabled() || !markdownConfig.isEnabled()) return message;

        ComponentRenderer renderer = ComponentRenderer.builder()
                .parseEmphasis(checkPerm(player, markdownConfig.getItalic()))
                .parseStrongEmphasis(checkPerm(player, markdownConfig.getBold()))
                .parseStrikethrough(checkPerm(player, markdownConfig.getStrikethrough()))
                .parseUnderline(checkPerm(player, markdownConfig.getUnderline()))
                .parseLinks(checkPerm(player, markdownConfig.getLinks()))
                .build();

        return parseMarkdown(renderer, message);
    }

    /**
     * Parses markdown in the given component and its children
     * @param component The component to parse markdown in
     * @return A styled version of the given component
     */
    private Component parseMarkdown(@NonNull ComponentRenderer renderer, @NonNull Component component) {
        Component newComp = component instanceof TextComponent text ?
                renderer.render(mdParser.parse(text.content()))
                        .style(b -> b.merge(component.style())) : component;

        return newComp.children(Stream.concat(
                newComp.children().stream(),
                component.children().stream()
                        .map(comp -> parseMarkdown(renderer, comp)))
                .toList());
    }

    private static boolean checkPerm(@NonNull ServerPlayer player, String perm) {
        //noinspection ConstantValue
        return perm == null || ChattixArch.hasPermission(player, perm, false);
    }
}
