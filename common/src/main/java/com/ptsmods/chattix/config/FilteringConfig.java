package com.ptsmods.chattix.config;

import lombok.Data;
import lombok.Getter;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.hjson.JsonObject;

import java.util.regex.Pattern;

@Data
public final class FilteringConfig {
    public static final FilteringConfig DEFAULT = new FilteringConfig(false, Pattern.compile("[\\w\\s.,&:+=\\-*/'\";?!@#$%<>À-ÖØ-öø-ÿ]*"),
            Pattern.compile("[^\\w\\s.,&:+=\\-*/'\";?!@#$%<>À-ÖØ-öø-ÿ]"));
    private final boolean enabled;
    private final Pattern pattern;
    private final Pattern negatedPattern;
    @Getter(lazy = true)
    private final TextReplacementConfig replacementConfig = createReplacementConfig();

    public static FilteringConfig fromJson(JsonObject object) {
        String pattern = object.getString("pattern", DEFAULT.getPattern().pattern().substring(0, DEFAULT.getPattern().pattern().length() - 1));
        return new FilteringConfig(object.getBoolean("enabled", false), Pattern.compile("[" + pattern + "]*"),
                Pattern.compile("[^" + pattern + "]"));
    }

    private TextReplacementConfig createReplacementConfig() {
        return TextReplacementConfig.builder()
                .match(getNegatedPattern())
                .replacement(builder -> builder.style(net.kyori.adventure.text.format.Style.style()
                        .decorate(TextDecoration.UNDERLINED)
                        .color(NamedTextColor.RED)
                        .build()))
                .build();
    }
}
