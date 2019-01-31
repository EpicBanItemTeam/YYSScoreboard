package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.data.ObjectiveData;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.io.IOException;
import java.util.*;

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

    private static List<ObjectiveData> scoreBoardData = new ArrayList<>();


    private ScoreBoardConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(YysScoreBoard.plugin.cfgDir.resolve("scoreboard.conf")).build();
        reload();
        save();
    }

    public static void reload() {
        noClear.clear();
        cache.clear();
        scoreBoardData.clear();
        loadNode();
        try {
            scoreBoardData.add(new ObjectiveData(cfg.getNode("scoreboard")));
        } catch (ObjectMappingException e) {
            YysScoreBoard.logger.warn("scoreboard config error!", e);
        }
    }

    public static void setPlayerScoreBoard(Player p) {
        Scoreboard sb;
        if (PlayerConfig.list.contains(p.getUniqueId())) {
            sb = p.getScoreboard();
            sb.getObjective(OBJECTIVE_NAME).ifPresent(sb::removeObjective);
        } else {
            sb = getPlayerScoreboard(p);
            setScoreBoard(sb, p);
            if (sb != p.getScoreboard()) {
                p.setScoreboard(sb);
            }
        }
    }

    public static void setPlayerScoreBoard(Collection<Player> players) {
        boolean setStatic = false;
        Scoreboard sb;

        for (Player p : players) {
            if (PlayerConfig.list.contains(p.getUniqueId())) {
                sb = p.getScoreboard();
                sb.getObjective(OBJECTIVE_NAME).ifPresent(sb::removeObjective);
            } else {
                sb = getPlayerScoreboard(p);
                if (sb == getStaticScoreBoard()) {
                    if (!setStatic) {
                        setStatic = true;
                        setScoreBoard(sb, p);
                    }
                } else {
                    setScoreBoard(sb, p);
                }
                if (sb != p.getScoreboard()) {
                    p.setScoreboard(sb);
                }
            }
        }
    }

    private static void setScoreBoard(Scoreboard sb, Player p) {
        Objective objective = sb.getObjective(OBJECTIVE_NAME).orElse(null);
        boolean shouldAdd = false;
        if (objective == null) {
            shouldAdd = true;
        }
        objective = scoreBoardData.get(0).setObjective(objective, p);
        if (shouldAdd) {
            sb.addObjective(objective);
        }
        sb.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);

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

    private static Scoreboard getPlayerScoreboard(Player p) {
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
