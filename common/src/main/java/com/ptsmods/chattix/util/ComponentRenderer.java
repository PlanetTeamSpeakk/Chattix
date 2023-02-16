package com.ptsmods.chattix.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.ins.Ins;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

@UtilityClass
public class ComponentRenderer {
    public static Component render(Node node) {
        Renderer renderer = new Renderer();
        renderer.render(node);

        return renderer.build();
    }

    private static class Renderer extends AbstractVisitor implements NodeRenderer {
        private final TextComponent.Builder comp = Component.text();
        private Style currentStyle = Style.empty();

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Set.of(
                    Document.class,
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
        public void visit(Emphasis emphasis) {
            decorate(TextDecoration.ITALIC, emphasis);
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            decorate(TextDecoration.BOLD, strongEmphasis);
        }

        @Override
        public void visit(Text text) {
            comp.append(Component.text(text.getLiteral()).style(currentStyle));
        }

        @Override
        public void visit(CustomNode customNode) {
            if (customNode instanceof Strikethrough) decorate(TextDecoration.STRIKETHROUGH, customNode);
            else if (customNode instanceof Ins) decorate(TextDecoration.UNDERLINED, customNode);
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
