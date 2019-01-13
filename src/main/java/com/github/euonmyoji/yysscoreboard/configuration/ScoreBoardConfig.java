package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.euonmyoji.yysscoreboard.YysScoreBoard.textManager;
import static com.github.euonmyoji.yysscoreboard.configuration.PluginConfig.*;

/**
 * @author yinyangshi
 */
public final class ScoreBoardConfig {
    public static final String NAME = "yyssbObjective";
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static Scoreboard staticScoreBoard;
    private static List<ScoreboardRawData> lines;
    private static String title;
    private static Set<UUID> noClear = new HashSet<>();


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
        loadNode();
        try {
            List<String> list = cfg.getNode("scoreboard", "lines").getList(TypeTokens.STRING_TOKEN, new ArrayList<String>() {{
                add("欢迎来到Gensokyo;;233");
                add("少女传教中:thwiki.cc;;9");
            }});
            lines = list.stream().map(new Function<String, ScoreboardRawData>() {
                private int slot = list.size() - 1;

                @Override
                public ScoreboardRawData apply(String s) {
                    String[] args = s.split(";;", 2);
                    String text = args[0];
                    int slot = args.length > 1 ? Integer.parseInt(args[1], 10) : this.slot;
                    if (slot < this.slot) {
                        this.slot = slot - 1;
                    }
                    return new ScoreboardRawData(text, slot);
                }
            }).collect(Collectors.toList());
            title = cfg.getNode("scoreboard", "title").getString("YYS Scoreboard");
        } catch (ObjectMappingException e) {
            YysScoreBoard.logger.warn("scoreboard config error!", e);
        }
    }

    public static void setPlayerScoreBoard(Player p) {
        if (PlayerConfig.list.contains(p.getUniqueId())) {
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
            setScoreBoard(sb, p);
            p.setScoreboard(sb);
        }
    }

    private static void setScoreBoard(Scoreboard sb, Player p) {
        Objective objective = sb.getObjective(NAME).orElse(null);
        boolean shouldAdd = false;
        if (objective == null) {
            shouldAdd = true;
        }
        objective = setObjective(objective, p);
        if (shouldAdd) {
            sb.addObjective(objective);
            sb.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
        }
    }


    private static Objective setObjective(@Nullable Objective objective, Player p) {
        if (objective == null) {
            objective = Objective.builder()
                    .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                    .name(NAME)
                    .criterion(Criteria.DUMMY)
                    .build();
        }
        objective.setDisplayName(textManager.toText(title, p));
        Map<Text, Score> map = objective.getScores();
        if (!noClear.contains(p.getUniqueId())) {
            map.values().forEach(objective::removeScore);
            noClear.add(p.getUniqueId());
            map = null;
        }
        if (hasSameScore && map != null) {
            map.values().forEach(objective::removeScore);
        }
        for (ScoreboardRawData data : lines) {
            Text text = textManager.toText(data.text, p);

            if (hasSameScore) {
                Score score = objective.getOrCreateScore(text);
                score.setScore(data.score);
            } else if (map != null) {
                Set<Text> keys = new HashSet<>();
                List<Score> cache = new ArrayList<>();
                map.forEach((text1, score) -> {
                    if (score.getScore() == data.score && !text.equals(text1)) {
                        keys.add(text1);
                        cache.add(score);
                    }
                });
                Score score = objective.getOrCreateScore(text);
                score.setScore(data.score);
                keys.forEach(map::remove);
                cache.forEach(objective::removeScore);
            } else {
                Score score = objective.getOrCreateScore(text);
                score.setScore(data.score);
            }
        }
        return objective;
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

    private static WeakHashMap<UUID, Scoreboard> cache = new WeakHashMap<>();

    private static Scoreboard getPlayerOnlyScoreboard(UUID uuid) {
        Scoreboard sb = cache.get(uuid);
        if (sb == null) {
            sb = Scoreboard.builder().build();
            cache.put(uuid, sb);
        }
        return sb;
    }

    private static class ScoreboardRawData {
        private String text;
        private int score;

        private ScoreboardRawData(String s, int slot) {
            this.text = s;
            this.score = slot;
        }
    }
}
