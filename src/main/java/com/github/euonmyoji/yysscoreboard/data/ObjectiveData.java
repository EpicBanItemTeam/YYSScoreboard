package com.github.euonmyoji.yysscoreboard.data;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.util.RandomDelay;
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
import static com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig.getObjectiveName;

/**
 * @author yinyangshi
 */
public class ObjectiveData {
    public final RandomDelay delay;
    private final List<ScoreRawData> lines;
    private final String title;

    public ObjectiveData(CommentedConfigurationNode node, int delay) throws ObjectMappingException {
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
        this.delay = new RandomDelay(node.getNode("delay").getString(delay + ""));
    }

    public Objective setObjective(@Nullable Objective objective, @Nullable Player p) {
        try (Timing timing = Timings.of(YysScoreBoard.plugin, "Get Objective")) {
            timing.startTimingIfSync();
            if (objective == null) {
                objective = Objective.builder()
                        .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                        .name(getObjectiveName(p))
                        .criterion(Criteria.DUMMY)
                        .build();
            }
            objective.setDisplayName(textManager.toText(title, p));
            Map<Text, Score> map = objective.getScores();
            if (hasSameScore) {
                for (Score value : map.values()) {
                    objective.removeScore(value);
                }
            } else {
                Iterator<Map.Entry<Text, Score>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Text, Score> entry = it.next();
                    if (lines.stream().allMatch(data -> data.score != entry.getValue().getScore())) {
                        objective.removeScore(entry.getValue());
                        it.remove();
                    }
                }
            }

            for (ScoreRawData data : lines) {
                Text text = textManager.toText(data.text, p);

                if (hasSameScore) {
                    Score score = objective.getOrCreateScore(text);
                    score.setScore(data.score);
                } else {
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
                    for (Text key : keys) {
                        map.remove(key);
                    }
                    for (Score value : cache) {
                        objective.removeScore(value);
                    }
                }
            }
        }
        return objective;
    }

    private static class ScoreRawData {
        private final String text;
        private final int score;

        private ScoreRawData(String s, int slot) {
            this.text = s;
            this.score = slot;
        }
    }
}
