package com.ptsmods.chattix.config.upgrades;

import org.hjson.JsonObject;

public class UpgradeV1 implements ConfigUpgrade {
    @Override
    public int sourceVersion() {
        return 1;
    }

    @Override
    public JsonObject upgrade(JsonObject config) {
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

        return config;
    }
}
