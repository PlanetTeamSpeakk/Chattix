package com.ptsmods.chattix.config;

import org.hjson.JsonObject;

import java.util.regex.Pattern;

public record FilteringConfig(boolean enabled, Pattern pattern, Pattern negatedPattern) {
    public static final FilteringConfig DEFAULT = new FilteringConfig(false, Pattern.compile("[\\w\\s.,&:+=\\-*/'\";?!@#$%<>À-ÖØ-öø-ÿ]*"),
            Pattern.compile("[^\\w\\s.,&:+=\\-*/'\";?!@#$%<>À-ÖØ-öø-ÿ]"));

    public static FilteringConfig fromJson(JsonObject object) {
        String pattern = object.getString("pattern", DEFAULT.pattern().pattern().substring(0, DEFAULT.pattern().pattern().length() - 1));
        return new FilteringConfig(object.getBoolean("enabled", false), Pattern.compile("[" + pattern + "]*"),
                Pattern.compile("[^" + pattern + "]"));
    }
}
