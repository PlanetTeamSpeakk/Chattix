package com.ptsmods.chattix.config.upgrades;

import org.hjson.JsonObject;

public interface ConfigUpgrade {
    int sourceVersion();

    JsonObject upgrade(JsonObject config);
}
