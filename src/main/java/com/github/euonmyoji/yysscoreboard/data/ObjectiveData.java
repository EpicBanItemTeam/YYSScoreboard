package com.github.euonmyoji.yysscoreboard.data;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.euonmyoji.yysscoreboard.YysScoreBoard.textManager;
import static com.github.euonmyoji.yysscoreboard.configuration.PluginConfig.hasSameScore;
import static com.github.euonmyoji.yysscoreboard.configuration.PluginConfig.noClear;
import static com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig.OBJECTIVE_NAME;

/**
 * @author yinyangshi
 */
public class ObjectiveData {
    private final List<ScoreRawData> lines;
    private final String title;

    public ObjectiveData(CommentedConfigurationNode node) throws ObjectMappingException {
        List<String> list = node.getNode("lines").getList(TypeTokens.STRING_TOKEN);
        lines = node.getNode("lines").getList(TypeTokens.STRING_TOKEN, Collections.emptyList())
                .stream().map(new Function<String, ScoreRawData>() {
                    private int slot = list.size() - 1;

                    @Override
                    public ScoreRawData apply(String s) {
                        String[] args = s.split(";;", 2);
                        String text = args[0];
                        int slot = args.length > 1 ? Integer.parseInt(args[1], 10) : this.slot;
                        if (slot <= this.slot) {
                            this.slot = slot - 1;
                        }
                        return new ScoreRawData(text, slot);
                    }
                }).collect(Collectors.toList());
        title = node.getNode("title").getString("YYS Scoreboard");
    }

    public Objective setObjective(@Nullable Objective objective, Player p) {
        if (objective == null) {
            objective = Objective.builder()
                    .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                    .name(OBJECTIVE_NAME)
                    .criterion(Criteria.DUMMY)
                    .build();
        }
        try {
            objective.setDisplayName(textManager.toText(title, p));
        } catch (NullPointerException e) {
            YysScoreBoard.logger.warn("I don't know why NPE, title:" + title + ", player:" + p);
        }
        Map<Text, Score> map = objective.getScores();
        if (!noClear.contains(p.getUniqueId())) {
            map.values().forEach(objective::removeScore);
            noClear.add(p.getUniqueId());
            map = null;
        }
        if (hasSameScore && map != null) {
            map.values().forEach(objective::removeScore);
        }
        for (ScoreRawData data : lines) {
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

    private static class ScoreRawData {
        private String text;
        private int score;

        private ScoreRawData(String s, int slot) {
            this.text = s;
            this.score = slot;
        }
    }
}
