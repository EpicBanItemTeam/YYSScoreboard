package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.data.ObjectiveData;
import com.github.euonmyoji.yysscoreboard.data.TabData;
import com.github.euonmyoji.yysscoreboard.manager.LanguageManager;
import com.github.euonmyoji.yysscoreboard.task.DisplayObjective;
import com.github.euonmyoji.yysscoreboard.task.DisplayTab;
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
        loader = HoconConfigurationLoader.builder()
                .setPath(path).build();
        if (Files.notExists(path)) {
            loadNode();
            try {
                //tab node//////////////////////////////////////////////
                CommentedConfigurationNode node = cfg.getNode("tabs", "example");
                node.getNode("header").getString("Header~");
                node.getNode("footer").getString("Footer~");
                node.getNode("prefix").getString("[prefix]");
                node.getNode("suffix").getString("[suffix]");
                node.getNode("delay").getInt(500);
                //tab node2//////////////////////////////////////////////


                //sb node///////////////////////////////////////////////
                node = cfg.getNode("scoreboards", "example");
                node.getNode("delay").getInt(20);
                node.getNode("lines").getList(TypeTokens.STRING_TOKEN, new ArrayList<String>() {{
                    add("&4少女祈祷中;;233");
                    add("&4Now Loading~;;16");
                    add("->thwiki.cc;;9");
                }});
                node.getNode("title").getString("Gensokyo Info(x)");
                //sb node2///////////////////////////////////////////////
                node = cfg.getNode("scoreboards", "example2");
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
        reload();
        LanguageManager.init();
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
                    scoreBoardData.add(new ObjectiveData(o2, updateTick));
                } catch (ObjectMappingException e) {
                    YysScoreBoard.logger.warn("scoreboard config error! where:", o.toString());
                    YysScoreBoard.logger.warn("scoreboard config error!", e);
                }
            });

            YysScoreBoard.plugin.setDisplayTask(new DisplayObjective(scoreBoardData));
        } catch (ObjectMappingException e) {
            YysScoreBoard.logger.warn("scoreboard config error!", e);
        }
        cfg.getNode("tabs").getChildrenMap().forEach((o, o2) -> tabData.add(new TabData(o2, updateTick)));

        YysScoreBoard.plugin.setDisplayTab(new DisplayTab(tabData));
        LanguageManager.reload();
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
