package com.ptsmods.chattix.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.mixin.MixinStyleAccessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class VanillaComponentSerializer implements ComponentSerializer<Component, Component, net.minecraft.network.chat.Component> {
    private static final VanillaComponentSerializer instance = new VanillaComponentSerializer();
    private static final Codec<CompoundTag, String, CommandSyntaxException, RuntimeException> NBT_CODEC = Codec.codec(TagParser::parseTag, Tag::toString);

    private VanillaComponentSerializer() {}

    public static VanillaComponentSerializer vanilla() {
        return instance;
    }

    @Override
    public @NotNull Component deserialize(net.minecraft.network.chat.@NotNull Component input) {
        ComponentBuilder<?, ?> out;
        net.minecraft.network.chat.Style style = input.getStyle();

        ComponentContents contents = input.getContents();
        if (contents == ComponentContents.EMPTY) out = Component.text();
        else if (contents instanceof LiteralContents literal) out = Component.text().content(literal.text());
        else if (contents instanceof TranslatableContents translatable) out = Component.translatable().key(translatable.getKey())
                .args(Arrays.stream(translatable.getArgs())
                        .map(o -> o instanceof net.minecraft.network.chat.Component c ? deserialize(c) : o instanceof Component kc ? kc : Component.text(String.valueOf(o)))
                        .toList());
        else if (contents instanceof KeybindContents keybind) out = Component.keybind().keybind(keybind.getName());
        else if (contents instanceof NbtContents nbt) {
            NBTComponentBuilder<?, ?> nbtOut;
            DataSource dataSource = nbt.getDataSource();
            if (dataSource instanceof BlockDataSource block) {
                out = nbtOut = Component.blockNBT().pos(BlockNBTComponent.Pos.fromString(block.posPattern()));
            } else if (dataSource instanceof EntityDataSource entity) {
                out = nbtOut = Component.entityNBT().selector(entity.selectorPattern());
            } else if (dataSource instanceof StorageDataSource storage) {
                out = nbtOut = Component.storageNBT().storage(Key.key(storage.id().toString(), ':'));
            } else throw new IllegalArgumentException("Unsupported NBT datasource type: " + dataSource.getClass().getName());

            nbtOut.nbtPath(nbt.getNbtPath())
                    .interpret(nbt.isInterpreting())
                    .separator(nbt.getSeparator()
                            .map(this::deserialize)
                            .orElse(null));
        } else if (contents instanceof ScoreContents score) out = Component.score()
                .name(score.getName())
                .objective(score.getObjective());
        else if (contents instanceof SelectorContents selector) out = Component.selector()
                .pattern(selector.getPattern())
                .separator(selector.getSeparator().map(this::deserialize).orElse(null));
        else throw new IllegalArgumentException("Unsupported component type: " + contents.getClass().getName());

        MixinStyleAccessor styleAccessor = (MixinStyleAccessor) style;
        out.style(Style.style()
                .color(Optional.ofNullable(style.getColor())
                        .map(net.minecraft.network.chat.TextColor::getValue)
                        .map(TextColor::color)
                        .orElse(null))
                .decoration(TextDecoration.BOLD, TextDecoration.State.byBoolean(styleAccessor.getBold()))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.byBoolean(styleAccessor.getItalic()))
                .decoration(TextDecoration.UNDERLINED, TextDecoration.State.byBoolean(styleAccessor.getUnderlined()))
                .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.byBoolean(styleAccessor.getStrikethrough()))
                .decoration(TextDecoration.OBFUSCATED, TextDecoration.State.byBoolean(styleAccessor.getObfuscated()))
                .build());

        input.getSiblings().stream()
                .map(this::deserialize)
                .forEach(out::append);

        return out.build();
    }

    @NotNull
    @Override
    public net.minecraft.network.chat.Component serialize(@NotNull Component component) {
        ComponentContents contents;

        if (component instanceof TextComponent text) contents = new LiteralContents(text.content());
        else if (component instanceof TranslatableComponent translatable) contents = new TranslatableContents(translatable.key(),
                translatable.args().stream()
                        .map(this::serialize)
                        .toArray(Object[]::new));
        else if (component instanceof KeybindComponent keybind) contents = new KeybindContents(keybind.keybind());
        else if (component instanceof NBTComponent<?,?> nbt) {
            DataSource dataSource;
            if (nbt instanceof BlockNBTComponent block) dataSource = new BlockDataSource(block.pos().asString());
            else if (nbt instanceof EntityNBTComponent entity) dataSource = new EntityDataSource(entity.selector());
            else if (nbt instanceof StorageNBTComponent storage) dataSource = new StorageDataSource(new ResourceLocation(storage.storage().namespace(), storage.storage().value()));
            else throw new IllegalArgumentException("Unsupported NBT component type: " + nbt.getClass().getName());

            contents = new NbtContents(nbt.nbtPath(), nbt.interpret(), Optional.ofNullable(nbt.separator()).map(this::serialize), dataSource);
        } else if (component instanceof ScoreComponent score) contents = new ScoreContents(score.name(), score.objective());
        else if (component instanceof SelectorComponent selector) contents = new SelectorContents(selector.pattern(), Optional.ofNullable(selector.separator()).map(this::serialize));
        else throw new IllegalArgumentException("Unsupported component type: " + component.getClass().getName());

        MutableComponent out = MutableComponent.create(contents);
        Style style = component.style();
        out.withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(style.color() == null ? null :
                        net.minecraft.network.chat.TextColor.fromRgb(Objects.requireNonNull(style.color()).value()))
                .withBold(fromState(style.decoration(TextDecoration.BOLD)))
                .withItalic(fromState(style.decoration(TextDecoration.ITALIC)))
                .withUnderlined(fromState(style.decoration(TextDecoration.UNDERLINED)))
                .withStrikethrough(fromState(style.decoration(TextDecoration.STRIKETHROUGH)))
                .withObfuscated(fromState(style.decoration(TextDecoration.OBFUSCATED)))
                .withHoverEvent(fromKyori(style.hoverEvent()))
                .withClickEvent(fromKyori(style.clickEvent())));

        component.children().stream()
                .map(this::serialize)
                .forEach(out::append);

        return out;
    }

    private Boolean fromState(TextDecoration.State state) {
        return state == TextDecoration.State.NOT_SET ? null : state == TextDecoration.State.TRUE;
    }

    private HoverEvent fromKyori(net.kyori.adventure.text.event.HoverEvent<?> hover) {
        if (hover == null) return null;

        net.kyori.adventure.text.event.HoverEvent.Action<?> action = hover.action();
        if (action == net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT)
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, serialize((Component) hover.value()));
        else if (action == net.kyori.adventure.text.event.HoverEvent.Action.SHOW_ENTITY) {
            net.kyori.adventure.text.event.HoverEvent.ShowEntity value = (net.kyori.adventure.text.event.HoverEvent.ShowEntity) hover.value();
            return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityTooltipInfo(
                    Registry.ENTITY_TYPE.get(new ResourceLocation(value.type().asString())), value.id(), value.name() == null ? null : serialize(value.name())));
        } else if (action == net.kyori.adventure.text.event.HoverEvent.Action.SHOW_ITEM) {
            net.kyori.adventure.text.event.HoverEvent.ShowItem value = (net.kyori.adventure.text.event.HoverEvent.ShowItem) hover.value();
            ItemStack stack = new ItemStack(Registry.ITEM.get(new ResourceLocation(value.item().asString())));
            BinaryTagHolder nbt = value.nbt();
            if (nbt != null) try {
                stack.setTag(nbt.get(NBT_CODEC));
            } catch (CommandSyntaxException e) {
                Chattix.LOG.error("Could not decode nbt for show item hover event.", e);
            }
            stack.setCount(value.count());

            return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack));
        } else throw new IllegalArgumentException("Hover event has an unknown action: " + action);
    }

    private ClickEvent fromKyori(net.kyori.adventure.text.event.ClickEvent click) {
        return click == null ? null : new ClickEvent(ClickEvent.Action.getByName(click.action().toString()), click.value());
    }
}
