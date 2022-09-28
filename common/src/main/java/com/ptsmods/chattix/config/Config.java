package com.ptsmods.chattix.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ptsmods.chattix.Chattix;
import com.ptsmods.chattix.config.upgrades.ConfigUpgrade;
import com.ptsmods.chattix.config.upgrades.UpgradeV1;
import dev.architectury.platform.Platform;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.world.entity.player.Player;
import org.hjson.*;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.ptsmods.chattix.Chattix.LOG;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {
    @Getter
    private static final Path configFile = Platform.getConfigFolder().resolve("chattix/config.hjson");
    public static final Config DEFAULT = new Config(1, FormattingConfig.DEFAULT, VicinityChatConfig.DEFAULT, MentionsConfig.DEFAULT, JoinLeaveConfig.DEFAULT);
    private static final Path ignoredPath = getConfigFile().resolveSibling("ignored/");
    private static final Path mutedPath = getConfigFile().resolveSibling("muted.json");
    private static final Int2ObjectMap<ConfigUpgrade> upgrades = new Int2ObjectLinkedOpenHashMap<>();
    static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
            .registerTypeHierarchyAdapter(Style .class, new Style.Serializer())
            .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
            .setPrettyPrinting()
            .create();
    @Getter
    private static Config instance = DEFAULT;
    private final int version;
    private final FormattingConfig formattingConfig;
    private final VicinityChatConfig vicinityChatConfig;
    private final MentionsConfig mentionsConfig;
    private final JoinLeaveConfig joinLeaveConfig;
    private final LoadingCache<UUID, Set<UUID>> ignored = CacheBuilder.newBuilder().build(new CacheLoader<>() {
        @NonNull
        @Override
        public Set<UUID> load(@NonNull UUID player) throws Exception {
            Path file = ignoredPath.resolve(player + ".json");
            if (!Files.exists(file)) return new LinkedHashSet<>();

            JsonArray ignored;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file)))) {
                ignored = JsonValue.readHjson(reader).asArray();
            }

            return StreamSupport.stream(ignored.spliterator(), false)
                    .map(JsonValue::asString)
                    .map(UUID::fromString)
                    .collect(LinkedHashSet::new, Set::add, Set::addAll);
        }
    });
    private final Map<UUID, Component> muted = new HashMap<>();

    static {
        upgrades.put(1, new UpgradeV1());
    }

    public static void load() {
        if (!ensureExists()) throw new RuntimeException();
        JsonObject config;
        try {
            config = JsonValue.readHjson(new BufferedReader(new InputStreamReader(Files.newInputStream(configFile)))).asObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int sourceVersion;
        int version = sourceVersion = config.getInt("config_version", 1);
        while (upgrades.containsKey(version)) {
            ConfigUpgrade upgrade = upgrades.get(version);
            LOG.info("Upgrading config from version " + upgrade.sourceVersion() + " to version " + (upgrade.sourceVersion() + 1));

            config = upgrade.upgrade(config);
            version = upgrade.sourceVersion() + 1;
        }

        if (sourceVersion != version) try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(configFile))))) {
            config.set("config_version", version);
            writer.write(config.toString(Stringify.HJSON));
            writer.flush();
        } catch (IOException e) {
            Chattix.LOG.error("Could not save upgraded config.", e);
        }

        FormattingConfig formattingConfig = config.get("formatting") == null ? FormattingConfig.DEFAULT : FormattingConfig.fromJson(config.get("formatting").asObject());
        VicinityChatConfig vicinityChatConfig = config.get("vicinity_chat") == null ? VicinityChatConfig.DEFAULT : VicinityChatConfig.fromJson(config.get("vicinity_chat").asObject());
        MentionsConfig mentionsConfig = config.get("mentions") == null ? MentionsConfig.DEFAULT : MentionsConfig.fromJson(config.get("mentions").asObject());
        JoinLeaveConfig joinLeaveConfig = config.get("join_leave_messages") == null ? JoinLeaveConfig.DEFAULT : JoinLeaveConfig.fromJson(config.get("join_leave_messages").asObject());

        instance = new Config(version, formattingConfig, vicinityChatConfig, mentionsConfig, joinLeaveConfig);

        if (Files.exists(mutedPath)) try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(mutedPath)))) {
            //noinspection UnstableApiUsage
            instance.muted.putAll(gson.fromJson(reader, new TypeToken<Map<UUID, Component>>() {}.getType()));
        } catch (IOException e) {
            Chattix.LOG.error("Could not read muted players file.", e);
        }
    }

    @SneakyThrows // Unlikely that Files#createDirectories(Path) will throw an exception
    private static boolean ensureExists() {
        if (!Files.exists(ignoredPath)) Files.createDirectory(ignoredPath);
        if (!Files.exists(configFile)) {
            Files.createDirectories(configFile.getParent());
            try (ReadableByteChannel rbc = Channels.newChannel(Objects.requireNonNull(Chattix.class.getClassLoader().getResourceAsStream("config.hjson")));
                 FileOutputStream outputStream = new FileOutputStream(configFile.toString()); FileChannel output = outputStream.getChannel()) {
                output.transferFrom(rbc, 0, Long.MAX_VALUE);
                return true;
            } catch (IOException e) {
                LOG.error("Could not store default config.", e);
                return false;
            }
        } else return true;
    }

    public boolean hasNotIgnored(Player player, Player target) {
        return !ignored.getUnchecked(player.getUUID()).contains(target.getUUID());
    }

    public void storeIgnoredFor(Player player) {
        Set<UUID> playerIgnored = ignored.getIfPresent(player.getUUID());
        if (playerIgnored == null) return;

        saveJson(playerIgnored, ignoredPath.resolve(player.getUUID() + ".json"), "ignored players for player " + player);
    }

    public boolean isMuted(Player player) {
        return muted.containsKey(player.getUUID());
    }

    public void mute(Player player, Component reason) {
        muted.put(player.getUUID(), reason);

        saveJson(muted, mutedPath, "muted players");
    }

    public void unmute(Player player) {
        muted.remove(player.getUUID());

        saveJson(muted, mutedPath, "muted players");
    }

    public Component getMuteReason(Player player) {
        return muted.get(player.getUUID());
    }

    private void saveJson(Object object, Path path, String name) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(path))))) {
            gson.toJson(object, writer);
            writer.flush();
        } catch (IOException e) {
            Chattix.LOG.error("Could not save " + name, e);
        }
    }
}
