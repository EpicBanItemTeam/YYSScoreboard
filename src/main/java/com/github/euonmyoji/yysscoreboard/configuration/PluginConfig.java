package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.manager.TaskManager;
import com.github.euonmyoji.yysscoreboard.task.DisplayNumber;
import com.google.common.base.Strings;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * @author yinyangshi
 */

public final class PluginConfig {
    private static final String LANGUAGE = "lang";
    private static final String STABLE_MODE = "stable";
    private static final String STATIC_MODE = "static";
    private static final String SAME_SCORE_MODE = "same-score";
    private static final String UPDATE_TICK = "update-interval-tick";
    private static final String DATA_DIR = "data-dir-path";
    private static final String CACHE_SCOREBOARD = "cache-scoreboard";
    public static Path cfgDir;
    public static Path defaultCfgDir;
    public static boolean isStaticMode = false;
    public static boolean asyncSidebar = false;
    public static boolean asyncDefault = false;
    public static boolean asyncTab = false;
    public static int goalCount = 9;
    public static boolean hasSameScore = false;
    public static int updateTick = 20;
    public static boolean isStableMode = true;
    static Set<UUID> noClear = new HashSet<>();
    static boolean cacheScoreboard = true;
    private static CommentedConfigurationNode cfg;
    private static CommentedConfigurationNode generalNode;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private PluginConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(defaultCfgDir.resolve("config.conf")).build();
        reload();
        setComment();
        save();
    }

    public static String getUsingLang() {
        return generalNode.getNode(LANGUAGE).getString(Locale.getDefault().toString());
    }

    public static void reload() {
        loadNode();
        generalNode.getNode(LANGUAGE).getString(Locale.getDefault().toString());
        CommentedConfigurationNode modes = generalNode.getNode("scoreboard-mode");
        isStableMode = modes.getNode(STABLE_MODE).getBoolean(true);
        isStaticMode = modes.getNode(STATIC_MODE).getBoolean(false);
        hasSameScore = modes.getNode(SAME_SCORE_MODE).getBoolean(false);
        cacheScoreboard = modes.getNode(CACHE_SCOREBOARD).getBoolean(true);
        asyncSidebar = modes.getNode("async-update", "sidebar").getBoolean(false);
        asyncTab = modes.getNode("async-update", "tab").getBoolean(false);
        asyncDefault = modes.getNode("async-update", "default").getBoolean(false);
        updateTick = generalNode.getNode(UPDATE_TICK).getInt(20);

        String path = generalNode.getNode(DATA_DIR).getString("default");
        cfgDir = "default".equals(path) ? defaultCfgDir : Paths.get(path);
        YysScoreBoard.logger.info("using data dir path:" + cfgDir);

        try {
            Files.createDirectories(cfgDir.resolve("PlayerData"));
            Files.createDirectories(cfgDir.resolve("scoreboards"));
            Files.createDirectories(cfgDir.resolve("tabs"));
        } catch (IOException e) {
            YysScoreBoard.logger.warn("create dir failed", e);
        }

        ////////////extra///////////
        cfg.getNode("extra", "self-name-prefix").getString("");
        goalCount = cfg.getNode("extra", "parallelGoal").getInt(9);
        String showNumber = cfg.getNode("extra", "showNumber").getString("");
        if (!Strings.isNullOrEmpty(showNumber)) {
            DisplayNumber task;
            switch (showNumber.toUpperCase()) {
                case "PING": {
                    task = new DisplayNumber(DisplayNumber.NumberSupplier.PING);
                    break;
                }
                case "HEALTH":
                case "HEALTHY": {
                    task = new DisplayNumber(DisplayNumber.NumberSupplier.HEALTHY);
                    break;
                }
                default: {
                    task = new DisplayNumber(new DisplayNumber.NumberSupplier.Default(showNumber));
                    break;
                }
            }
            TaskManager.registerTask(task);
        }
    }

    public static String getSelfNamePrefix() {
        return cfg.getNode("extra", "self-name-prefix").getString("");
    }

    private static void save() {
        try {
            loader.save(cfg);
        } catch (IOException e) {
            YysScoreBoard.logger.warn("error when saving plugin config", e);
        }
    }

    private static void loadNode() {
        try {
            cfg = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException e) {
            YysScoreBoard.logger.warn("load plugin config failed", e);
        }
        generalNode = cfg.getNode("general");
    }

    private static void setComment() {
        generalNode.getNode(UPDATE_TICK).setComment(generalNode.getNode(UPDATE_TICK).getComment()
                .orElse("The default tab&scoreboard delay|update tick"));

        generalNode.getNode("scoreboard-mode").setComment("Plugin work mode" +
                "\n·async-something: Do something async (not in main thread)" +
                "\n·cache-scoreboard: Cache player's only scoreboard in a hash map" +
                "\n·same-score: Scoreboard whether have same score or not" +
                "\n·stable: GetScoreboard from player using instead of building new one" +
                "\n·static: Player may use the same scoreboard and won't parse player placeholder");

        cfg.getNode("extra", "parallelGoal").setComment("The size of something is bigger than this will parallel");
    }
}
