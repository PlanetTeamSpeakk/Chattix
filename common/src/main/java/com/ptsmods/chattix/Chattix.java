package com.ptsmods.chattix;

import com.ptsmods.chattix.commands.*;
import com.ptsmods.chattix.config.Config;
import com.ptsmods.chattix.placeholder.Placeholders;
import com.ptsmods.chattix.util.ChattixArch;
import com.ptsmods.chattix.util.VanillaComponentSerializer;
import dev.architectury.event.events.common.ChatEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public class Chattix {
    public static final String MOD_ID = "chattix";
    public static final Logger LOG = LogManager.getLogger();
    private static final Path chatDisabledFile = Config.getConfigFile().resolveSibling("chat_disabled");
    private static boolean formattingMessageArgument = false;
    @Getter
    private static boolean chatDisabled = Files.exists(chatDisabledFile);

    public static void init() {
        Config.load();

        // Workaround for a bug (I think) in Forge.
        // Currently, whenever you try to load classes in an event (or perhaps specifically async ones), the wrong
        // classloader is used. Instead of the classloader that loaded this mod file, the built-in system classloader is
        // used which does not know about this mod file nor its contents and thus cannot find its classes.
        // To work around this, we load all necessary classes by formatting an empty message beforehand.
        format(null, net.kyori.adventure.text.Component.empty());
        Placeholders.init(); // We call this for the same reason.

        ChattixArch.registerPermission("chattix.bypass", false);

        ChatEvent.DECORATE.register((player, component) -> {
            if (player == null) return;

            //noinspection ConstantConditions // IntelliJ does not seem to recognise chatDisabled changes
            if (chatDisabled && !ChattixArch.hasPermission(player, "chattix.bypass"))
                component.set(Component.literal("Chat is currently disabled!")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            else if (Config.getInstance().isMuted(player))
                component.set(Component.literal("You cannot speak as you are muted! Reason: ")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                        .append(Config.getInstance().getMuteReason(player)));
            else if (!formattingMessageArgument && Config.getInstance().getFormattingConfig().isEnabled())
                component.set(format(player, VanillaComponentSerializer.vanilla().deserialize(component.get())));
        });

        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            ChattixCommand.register(dispatcher);
            IgnoreCommand.register(dispatcher);
            MentionsCommand.register(dispatcher);
            ChatCommand.register(dispatcher);
            if (selection == Commands.CommandSelection.DEDICATED) MuteCommand.register(dispatcher);
        });

        PlayerEvent.PLAYER_JOIN.register(player -> Config.getInstance().getIgnored().refresh(player.getUUID()));
        PlayerEvent.PLAYER_QUIT.register(player -> Config.getInstance().getIgnored().invalidate(player.getUUID()));
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message) {
        return format(player, message, Config.getInstance().getFormattingConfig().getActiveFormatFor(player));
    }

    public static Component format(ServerPlayer player, net.kyori.adventure.text.Component message, String format) {
        TextReplacementConfig placeholderReplacement = Placeholders.createReplacementConfig(player, message);
        MiniMessage miniMessage = MiniMessage.builder()
                .tags(TagResolver.standard())
                .postProcessor(post -> post.replaceText(placeholderReplacement))
                .build();

        return VanillaComponentSerializer.vanilla().serialize(miniMessage.deserialize(format));
    }

    public static void setFormattingMessageArgument(boolean formattingMessageArgument) {
        Chattix.formattingMessageArgument = formattingMessageArgument;
    }

    @SneakyThrows
    public static void setChatDisabled(boolean chatDisabled) {
        Chattix.chatDisabled = chatDisabled;

        if (chatDisabled && !Files.exists(chatDisabledFile)) Files.createFile(chatDisabledFile);
        else if (!chatDisabled) Files.deleteIfExists(chatDisabledFile);
    }
}
