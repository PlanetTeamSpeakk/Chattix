package com.ptsmods.chattix.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ptsmods.chattix.Chattix;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.hjson.JsonObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class MentionsConfig {
    public static final MentionsConfig DEFAULT = new MentionsConfig(true, new ResourceLocation("minecraft:block.note_block.bell"), new HashSet<>());
    private static final Path toggledPath = Config.getConfigFile().resolveSibling("mentions_toggled.json");
    private final boolean enabled;
    private final ResourceLocation sound;
    private final Set<UUID> toggled;

    static MentionsConfig fromJson(JsonObject object) {
        JsonArray toggled;

        if (Files.exists(toggledPath)) try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(toggledPath)))) {
            toggled = new Gson().fromJson(reader, JsonArray.class);
        } catch (IOException e) {
            Chattix.LOG.error("Could not load toggled mentions", e);
            toggled = new JsonArray();
        } else toggled = new JsonArray();

        ResourceLocation sound = new ResourceLocation(object.getString("sound", DEFAULT.getSound().toString()));
        if (!BuiltInRegistries.SOUND_EVENT.containsKey(sound)) {
            Chattix.LOG.error("No sound with id " + sound + " appears to exist.");
            sound = DEFAULT.getSound();
        }

        return new MentionsConfig(object.getBoolean("enabled", true),
                sound, StreamSupport.stream(toggled.spliterator(), false)
                        .map(JsonElement::getAsString)
                        .map(UUID::fromString)
                        .collect(Collectors.toSet()));
    }

    public void toggleFor(Player player) {
        if (toggled.contains(player.getUUID())) toggled.remove(player.getUUID());
        else toggled.add(player.getUUID());

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(toggledPath))))) {
            Config.gson.toJson(toggled, writer);
            writer.flush();
        } catch (IOException e) {
            Chattix.LOG.error("Could not save toggled mentions.", e);
        }
    }

    public boolean isEnabledFor(Player player) {
        return isEnabled() ^ toggled.contains(player.getUUID());
    }
}
