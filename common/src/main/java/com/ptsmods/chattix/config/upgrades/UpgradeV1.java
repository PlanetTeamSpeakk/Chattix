package com.ptsmods.chattix.config.upgrades;

import net.kyori.adventure.text.format.NamedTextColor;
import org.hjson.JsonArray;
import org.hjson.JsonObject;

public class UpgradeV1 implements ConfigUpgrade {
    @Override
    public int sourceVersion() {
        return 1;
    }

    @Override
    public JsonObject upgrade(JsonObject config) {
        JsonObject moderationConfig = new JsonObject();
        JsonObject slowModeConfig = new JsonObject();
        slowModeConfig.set("enabled", false);
        slowModeConfig.set("cooldown", 3);
        moderationConfig.set("slow_mode", slowModeConfig);

        JsonObject welcomingConfig = new JsonObject();
        welcomingConfig.set("enabled", true);
        welcomingConfig.set("format", "<light_purple>Welcome to the server, %name%!</light_purple>");
        welcomingConfig.set("broadcast", true);
        moderationConfig.set("welcoming", welcomingConfig);

        JsonObject linksConfig = new JsonObject();
        linksConfig.set("enabled", true);
        linksConfig.set("requires_permission", true);
        linksConfig.set("format", "<blue><u>%url%</u></blue>");
        moderationConfig.set("links", linksConfig);

        moderationConfig.set("similarity", 1.0);
        config.set("moderation", moderationConfig);

        JsonObject joinLeaveMessagesConfig = new JsonObject();
        joinLeaveMessagesConfig.set("enabled", true);
        joinLeaveMessagesConfig.set("join_format", "<yellow>%name% has joined the game</yellow>");
        joinLeaveMessagesConfig.set("join_changed_name_format", "<yellow>%name% has joined the game (was %old_name%)</yellow>");
        joinLeaveMessagesConfig.set("leave_format", "<yellow>%name% has left the game</yellow>");
        config.set("join_leave_messages", joinLeaveMessagesConfig);

        JsonObject filteringConfig = new JsonObject();
        filteringConfig.set("enabled", false);
        filteringConfig.set("pattern", "\\\\w\\\\s.,&:+=\\\\-*/'\\\";?!@#$%<>À-ÖØ-öø-ÿ");
        config.set("filtering", filteringConfig);

        JsonObject vicinityChatConfig = config.get("vicinity_chat") == null ? null : config.get("vicinity_chat").asObject();
        if (vicinityChatConfig != null) {
            JsonObject localChatConfig = new JsonObject();
            localChatConfig.set("enabled", true);
            localChatConfig.set("default", true);
            localChatConfig.set("prefix", "<dark_green><b>LOCAL</b></dark_green> ");
            vicinityChatConfig.set("local_chat", localChatConfig);
        }

        JsonObject formattingConfig = config.get("formatting") == null ? null : config.get("formatting").asObject();
        if (formattingConfig != null) {
            JsonObject markdownConfig = new JsonObject();
            markdownConfig.set("enabled", false);
            markdownConfig.set("bold", "chattix.markdown.basic");
            markdownConfig.set("italic", "chattix.markdown.basic");
            markdownConfig.set("strikethrough", "chattix.markdown.strikethrough");
            markdownConfig.set("underline", "chattix.markdown.underline");
            markdownConfig.set("links", "chattix.markdown.links");
            formattingConfig.set("markdown", markdownConfig);


            JsonObject chatFormattingConfig = new JsonObject();

            JsonArray basic = new JsonArray();
            NamedTextColor.NAMES.keys().stream()
                    .filter(s -> !"dark_blue".equals(s) && !"black".equals(s))
                    .map(String::toUpperCase)
                    .forEach(basic::add);
            chatFormattingConfig.set("chattix.chatformatting.basic", basic);

            chatFormattingConfig.set("chattix.chatformatting.dark", new JsonArray().add("DARK_BLUE").add("BLACK"));
            chatFormattingConfig.set("chattix.chatformatting.hex", new JsonArray().add("HEX"));
            chatFormattingConfig.set("chattix.chatformatting.decorations", new JsonArray()
                    .add("BOLD").add("UNDERLINED").add("ITALIC").add("STRIKETHROUGH"));
            chatFormattingConfig.set("chattix.chatformatting.obfuscation", new JsonArray().add("OBFUSCATED"));

            formattingConfig.set("chat_formatting", chatFormattingConfig);
        }

        return config;
    }
}
