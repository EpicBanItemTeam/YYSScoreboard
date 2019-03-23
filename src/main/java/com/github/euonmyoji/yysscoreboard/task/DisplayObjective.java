package com.github.euonmyoji.yysscoreboard.task;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.GlobalPlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.data.ObjectiveData;
import com.github.euonmyoji.yysscoreboard.manager.TaskManager;
import com.github.euonmyoji.yysscoreboard.util.Pair;
import com.github.euonmyoji.yysscoreboard.util.RandomID;
import com.github.euonmyoji.yysscoreboard.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig.OBJECTIVE_NAME;
import static com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig.getPlayerScoreboard;

/**
 * @author yinyangshi
 */
public class DisplayObjective implements IDisplayTask {
    private final String id;
    private final List<ObjectiveData> data;
    private final RandomID randomID;
    private int index = 0;
    private ObjectiveData cur;
    private volatile boolean running;

    public DisplayObjective(String id, List<ObjectiveData> data, RandomID randomID) {
        this.id = id;
        this.data = data;
        running = !data.isEmpty();
        cur = running ? data.get(0) : null;
        this.randomID = randomID;
    }

    @Override
    public void run() {
        if (running) {
            Task.Builder builder = Task.builder().execute(this);
            try {
                cur = data.get(index);
                if (PluginConfig.isStaticMode) {
                    ObjectiveData curData = data.get(index);
                    Util.getStream(Sponge.getServer().getOnlinePlayers())
                            .filter(p -> !GlobalPlayerConfig.list.contains(p.getUniqueId())
                                    && id.equals(TaskManager.usingCache.get(p.getUniqueId()).first))
                            .map(player -> {
                                Scoreboard sb = player.getScoreboard();
                                Objective objective = sb.getObjective(OBJECTIVE_NAME).orElse(null);
                                if (objective == null) {
                                    objective = cur.setObjective(null, player);
                                    sb.addObjective(objective);
                                    sb.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
                                    return null;
                                }
                                return objective;
                            })
                            .distinct()
                            .filter(Objects::nonNull)
                            .forEach(objective -> curData.setObjective(objective, null));
                } else {
                    Util.getStream(Sponge.getServer().getOnlinePlayers())
                            .filter(p -> !GlobalPlayerConfig.list.contains(p.getUniqueId())
                                    && id.equals(TaskManager.usingCache.get(p.getUniqueId()).first))
                            .forEach(this::setupPlayer);
                }

                if (PluginConfig.asyncSidebar) {
                    builder.async();
                }
                builder.delayTicks(data.get(index).delay.getDelay());
                if (++index >= data.size()) {
                    index = 0;
                    if (randomID != null) {
                        for (Pair<String, String> value : TaskManager.usingCache.values()) {
                            if (value.first.equals(id) && !value.immutable) {
                                value.first = randomID.getID();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                YysScoreBoard.logger.warn("something wrong", e);
            }
            builder.submit(YysScoreBoard.plugin);
        }
    }

    @Override
    public void setupPlayer(Player p) {
        if (running) {
            Scoreboard sb;
            if (!GlobalPlayerConfig.list.contains(p.getUniqueId())) {
                sb = getPlayerScoreboard(p);

                if (sb != p.getScoreboard()) {
                    p.setScoreboard(sb);
                }

                Optional<Objective> optObjective = sb.getObjective(OBJECTIVE_NAME);
                if (optObjective.isPresent()) {
                    cur.setObjective(optObjective.get(), p);
                } else {
                    Objective objective = cur.setObjective(null, p);
                    sb.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
                }
            }
        }
    }


    @Override
    public void cancel() {
        running = false;
    }
}
