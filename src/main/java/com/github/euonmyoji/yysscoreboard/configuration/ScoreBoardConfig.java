package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.data.ObjectiveData;
import com.github.euonmyoji.yysscoreboard.data.TabData;
import com.github.euonmyoji.yysscoreboard.task.DisplayScoreboard;
import com.github.euonmyoji.yysscoreboard.task.DisplayTab;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static Scoreboard staticScoreBoard;
    private static WeakHashMap<UUID, Scoreboard> cache = new WeakHashMap<>();


    private ScoreBoardConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        Path path = YysScoreBoard.plugin.cfgDir.resolve("scoreboard.conf");
        boolean virtual = Files.notExists(path);
        loader = HoconConfigurationLoader.builder()
                .setPath(path).build();
        reload();
        if (virtual) {
            CommentedConfigurationNode tabNode = cfg.getNode("tabs", "example");
            tabNode.getNode("header").getString("Header~");
            tabNode.getNode("footer").getString("Footer~");
            tabNode.getNode("delay").getInt(500);
        }
        save();
    }

    public static void reload() {
        noClear.clear();
        cache.clear();
        List<ObjectiveData> scoreBoardData = new ArrayList<>();
        List<TabData> tabData = new ArrayList<>();
        loadNode();
        try {
            CommentedConfigurationNode oldSb = cfg.getNode("scoreboard");
            if (!oldSb.isVirtual()) {
                scoreBoardData.add(new ObjectiveData(oldSb, updateTick));

            }
            cfg.getNode("scoreboards").getChildrenMap().forEach((o, o2) -> {
                try {
                    scoreBoardData
                            .add(new ObjectiveData(o2, updateTick));
                } catch (ObjectMappingException e) {
                    YysScoreBoard.logger.warn("scoreboard config error! where:", o.toString());
                    YysScoreBoard.logger.warn("scoreboard config error!", e);
                }
            });

            YysScoreBoard.plugin.setDisplayTask(new DisplayScoreboard(scoreBoardData));
        } catch (ObjectMappingException e) {
            YysScoreBoard.logger.warn("scoreboard config error!", e);
        }
        cfg.getNode("tabs").getChildrenMap().forEach((o, o2) -> tabData.add(new TabData(o2, updateTick)));

        YysScoreBoard.plugin.setDisplayTab(new DisplayTab(tabData));
    }

    public static void setPlayerScoreboard(Player p) {
        Scoreboard sb;
        if (PlayerConfig.list.contains(p.getUniqueId())) {
            sb = p.getScoreboard();
            sb.getObjective(OBJECTIVE_NAME).ifPresent(sb::removeObjective);
        } else {
            sb = getPlayerScoreboard(p);

            if (sb != p.getScoreboard()) {
                p.setScoreboard(sb);
            }
        }

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
        }
    }

    public static Scoreboard getStaticScoreBoard() {
        if (staticScoreBoard == null) {
            synchronized (ScoreBoardConfig.class) {
                if (staticScoreBoard == null) {
                    staticScoreBoard = Scoreboard.builder().build();
                    YysScoreBoard.logger.info("static scoreboard is removed by unknown reason, rebuild new one");
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
}
