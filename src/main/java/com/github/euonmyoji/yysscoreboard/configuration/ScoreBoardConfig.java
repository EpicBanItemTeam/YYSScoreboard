package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.data.ObjectiveData;
import com.github.euonmyoji.yysscoreboard.data.TabData;
import com.github.euonmyoji.yysscoreboard.manager.LanguageManager;
import com.github.euonmyoji.yysscoreboard.manager.TaskManager;
import com.github.euonmyoji.yysscoreboard.task.DisplayObjective;
import com.github.euonmyoji.yysscoreboard.task.DisplayTab;
import com.github.euonmyoji.yysscoreboard.util.RandomID;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.util.TypeTokens;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import static com.github.euonmyoji.yysscoreboard.configuration.PluginConfig.*;

/**
 * @author yinyangshi
 */
public final class ScoreBoardConfig {
    public static final String OBJECTIVE_NAME = "yyssbObjective";
    private static final int CONFIG_VERSION = 1;
    private static final String VERSION_KEY = "version";
    private static Path cfgPath;
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static Scoreboard staticScoreBoard;
    private static WeakHashMap<UUID, Scoreboard> cache = new WeakHashMap<>();

    private ScoreBoardConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        reload();
        LanguageManager.init();
    }

    public static void reload() {
        cfgPath = cfgDir.resolve("scoreboard.conf");
        loader = HoconConfigurationLoader.builder()
                .setPath(cfgPath).build();
        if (Files.notExists(cfgPath)) {
            loadNode();
            setExample();
        } else {
            loadNode();
            checkUpdate();
        }
        TaskManager.clear();
        noClear.clear();
        cache.clear();
        loadNode();
        final String settingsKey = "settings";
        cfg.getNode("scoreboards").getChildrenMap().forEach((o, task) -> {
            String id = o.toString();
            List<ObjectiveData> scoreBoardData = new ArrayList<>();

            task.getChildrenMap().forEach((o1, o2) -> {
                try {
                    if (!o1.toString().equals(settingsKey)) {
                        scoreBoardData.add(new ObjectiveData(o2, updateTick));
                    }
                } catch (ObjectMappingException e) {
                    YysScoreBoard.logger.warn("scoreboard config error! where:", o.toString());
                    YysScoreBoard.logger.warn("scoreboard config error!", e);
                }
            });
            RandomID randomID = null;
            if (!cfg.getNode(settingsKey).isVirtual()) {
                randomID = new RandomID(cfg.getNode(settingsKey, "next").getString());
            }
            TaskManager.registerTask(id, new DisplayObjective(id, scoreBoardData, randomID));
        });
        cfg.getNode("tabs").getChildrenMap().forEach((o, task) -> {
            String id = o.toString();
            List<TabData> tabData = new ArrayList<>();
            task.getChildrenMap().forEach((o1, o2) -> {
                if (!o1.toString().equals(settingsKey)) {
                    tabData.add(new TabData(o2, updateTick));
                }
            });
            RandomID randomID = null;
            if (!cfg.getNode(settingsKey).isVirtual()) {
                randomID = new RandomID(cfg.getNode(settingsKey, "next").getString());
            }
            TaskManager.registerTask(id, new DisplayTab(id, tabData, randomID));
        });
        LanguageManager.reload();
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
            YysScoreBoard.logger.warn("load scoreboard config failed", e);
            if (cfg == null) {
                throw new RuntimeException("scoreboard config load failed (cfg is null)", e);
            }
        }
    }

    public static Scoreboard getStaticScoreBoard() {
        if (staticScoreBoard == null) {
            synchronized (ScoreBoardConfig.class) {
                if (staticScoreBoard == null) {
                    staticScoreBoard = Scoreboard.builder().build();
                    YysScoreBoard.logger.info("static scoreboard is null, rebuild new one");
                }
            }
        }
        return staticScoreBoard;
    }

    public static Scoreboard getPlayerScoreboard(Player p) {
        Scoreboard sb = isStableMode ? p.getScoreboard() : isStaticMode ?
                getStaticScoreBoard() : getPlayerOnlyScoreboard(p.getUniqueId());
        if (sb == null) {
            YysScoreBoard.logger.info("the player {} scoreboard is null, try to solve it now", p.getName());
            if (isStaticMode) {
                sb = getStaticScoreBoard();
            } else {
                sb = Scoreboard.builder().build();
            }
        }
        return sb;
    }

    private static Scoreboard getPlayerOnlyScoreboard(UUID uuid) {
        if (cacheScoreboard) {
            Scoreboard sb = cache.get(uuid);
            if (sb == null) {
                sb = Scoreboard.builder().build();
                cache.put(uuid, sb);
            }
            return sb;
        } else {
            return Scoreboard.builder().build();
        }
    }

    private static void setExample() {
        cfg.getNode(VERSION_KEY).setValue(CONFIG_VERSION);
        try {
            //tab node//////////////////////////////////////////////
            CommentedConfigurationNode node = cfg.getNode("tabs", "main", "example");
            node.getNode("header").getString("Header~");
            node.getNode("footer").getString("Footer~");
            node.getNode("prefix").getString("[prefix]");
            node.getNode("suffix").getString("[suffix]");
            node.getNode("delay").getInt(100);
            //tab node2//////////////////////////////////////////////


            //sb node///////////////////////////////////////////////
            node = cfg.getNode("scoreboards", "main", "example");
            node.getNode("delay").getInt(20);
            node.getNode("lines").getList(TypeTokens.STRING_TOKEN, new ArrayList<String>() {{
                add("&4少女祈祷中;;233");
                add("&4Now Loading~;;16");
                add("->thwiki.cc;;9");
            }});
            node.getNode("title").getString("Gensokyo Info(x)");
            //sb node2///////////////////////////////////////////////
            node = cfg.getNode("scoreboards", "main", "example2");
            node.getNode("delay").getInt(20);
            node.getNode("lines").getList(TypeTokens.STRING_TOKEN, new ArrayList<String>() {{
                add("&2少女祈祷中;;233");
                add("&2Now Loading~;;16");
                add("-->thwiki.cc;;9");
            }});
            node.getNode("title").getString("Gensokyo Info(x)");
        } catch (ObjectMappingException e) {
            YysScoreBoard.logger.warn("wtf", e);
        }
        save();
    }

    private static void checkUpdate() {
        int version = cfg.getNode(VERSION_KEY).getInt(0);
        if (version != CONFIG_VERSION) {
            YysScoreBoard.logger.warn("The config is out-of-date (version :0) and latest version is ", CONFIG_VERSION);
            YysScoreBoard.logger.warn("backup config now");

            Path backupDir = defaultCfgDir.resolve("oldConfig");
            try {
                Files.createDirectories(backupDir);
                Path backupCfgFile = backupDir.resolve("V" + version + "scoreboard.conf");
                Files.copy(cfgPath, backupCfgFile, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                YysScoreBoard.logger.error("backup config error and won't update config (may cause some bug)", e);
                return;
            }

            cfg = loader.createEmptyNode(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            setExample();
        }
    }
}
