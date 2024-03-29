package com.ptsmods.chattix.util;

import lombok.Builder;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.ins.Ins;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;

import java.util.Collection;
import java.util.Set;
import java.util.function.UnaryOperator;

@Builder(builderClassName = "Builder")
public class ComponentRenderer {
    private boolean parseEmphasis, parseStrongEmphasis, parseStrikethrough, parseUnderline, parseLinks;
    @Singular
    private Collection<UnaryOperator<TextComponent>> postProcessors;

    public Component render(Node node) {
        Renderer renderer = new Renderer();
        renderer.render(node);

        return renderer.build();
    }

    private class Renderer extends AbstractVisitor implements NodeRenderer {
        private final TextComponent.Builder comp = Component.text();
        private Style currentStyle = Style.empty();

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Set.of(
                    Document.class,
                    Heading.class,
                    Link.class,
                    Text.class,
                    Emphasis.class,
                    StrongEmphasis.class,
                    Strikethrough.class,
                    Ins.class);
        }

        public Component build() {
            return comp.build();
        }

        @Override
        public void render(Node node) {
            node.accept(this);
        }

        @Override
        public void visit(Document document) {
            visitChildren(document);
        }

        @Override
        public void visit(Heading heading) {
            // Restore heading format
            append("#".repeat(heading.getLevel()) + " ");
            visitChildren(heading);
        }

        @Override
        public void visit(Link link) {
            if (parseLinks) {
                Style old = currentStyle;
                currentStyle = currentStyle
                        .decorate(TextDecoration.UNDERLINED)
                        .color(NamedTextColor.BLUE)
                        .clickEvent(ClickEvent.openUrl(link.getDestination()));
                visitChildren(link);
                currentStyle = old;
            } else {
                append("[");
                visitChildren(link);
                append("]");
                append("(" + link.getDestination() + ")");
            }
        }

        @Override
        public void visit(Code code) {
            append('`' + code.getLiteral() + '`');
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            append("```" + fencedCodeBlock.getLiteral() + "```");
        }

        @Override
        public void visit(Emphasis emphasis) {
            if (parseEmphasis) decorate(TextDecoration.ITALIC, emphasis);
            else {
                append("*");
                visitChildren(emphasis);
                append("*");
            }
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            if (parseStrongEmphasis) decorate(TextDecoration.BOLD, strongEmphasis);
            else {
                append("**");
                visitChildren(strongEmphasis);
                append("**");
            }
        }

        @Override
        public void visit(Text text) {
            append(text.getLiteral());
        }

        @Override
        public void visit(CustomNode customNode) {
            if (customNode instanceof Strikethrough)
                if (parseStrikethrough) decorate(TextDecoration.STRIKETHROUGH, customNode);
                else {
                    append("~");
                    visitChildren(customNode);
                    append("~");
                }
            else if (customNode instanceof Ins)
                if (parseUnderline) decorate(TextDecoration.UNDERLINED, customNode);
                else {
                    append("++");
                    visitChildren(customNode);
                    append("++");
                }
        }

        private void append(String text) {
            TextComponent textComp = Component.text(text).style(currentStyle);
            for (UnaryOperator<TextComponent> postProcessor : postProcessors) textComp = postProcessor.apply(textComp);

            comp.append(textComp);
        }

        private void decorate(TextDecoration decoration, Node parent) {
            // Apply decoration
            TextDecoration.State state = currentStyle.decoration(decoration);
            currentStyle = currentStyle.decorate(decoration);

            // Visit children (if it has a text child, which it probably does,
            // this will be appended with the proper style)
            visitChildren(parent);

            // Reset decoration
            currentStyle = currentStyle.decoration(decoration, state);
        }
    }
}
