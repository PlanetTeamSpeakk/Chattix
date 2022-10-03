package com.ptsmods.chattix.config.upgrades;

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

        return config;
    }
}
