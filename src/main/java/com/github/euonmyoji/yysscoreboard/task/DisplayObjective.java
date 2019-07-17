package com.github.euonmyoji.yysscoreboard.task;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.GlobalPlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.data.DisplayIDData;
import com.github.euonmyoji.yysscoreboard.data.ObjectiveData;
import com.github.euonmyoji.yysscoreboard.manager.TaskManager;
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
import java.util.stream.Stream;

import static com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig.getObjectiveName;
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
    private int errors = 0;
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
                Stream<Player> stream = Util.getStream(Sponge.getServer().getOnlinePlayers())
                        .filter(p -> {
                            if (!GlobalPlayerConfig.list.contains(p.getUniqueId())) {
                                DisplayIDData pair = TaskManager.usingCache.get(p.getUniqueId());
                                return pair != null && id.equals(TaskManager.usingCache.get(p.getUniqueId()).objectiveID);
                            }
                            return false;
                        });
                if (PluginConfig.isStaticMode) {
                    stream.map(player -> {
                        Scoreboard sb = player.getScoreboard();
                        Objective objective = sb.getObjective(getObjectiveName(player)).orElse(null);
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
                            .forEach(objective -> cur.setObjective(objective, null));
                } else {
                    stream.forEach(this::setupPlayer);
                }

                if (PluginConfig.asyncSidebar) {
                    builder.async();
                }
                builder.delayTicks(cur.delay.getDelay());
                if (++index >= data.size()) {
                    index = 0;
                    if (randomID != null) {
                        for (DisplayIDData value : TaskManager.usingCache.values()) {
                            if (value.objectiveID.equals(id) && !value.immutable) {
                                if (value.once) {
                                    value.objectiveID = randomID.getID();
                                    value.once = false;
                                } else {
                                    value.once = true;
                                }
                            }
                        }
                    }
                }
                errors = 0;
            } catch (Throwable e) {
                YysScoreBoard.logger.warn("something wrong while displaying objective", e);
                if (++errors > 1) {
                    YysScoreBoard.logger.warn("error twice continually, canceling the task");
                    cancel();
                }
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

                Optional<Objective> optObjective = sb.getObjective(getObjectiveName(p));
                if (optObjective.isPresent()) {
                    cur.setObjective(optObjective.get(), p);
                    if (!sb.getObjective(DisplaySlots.SIDEBAR).isPresent()) {
                        sb.updateDisplaySlot(optObjective.get(), DisplaySlots.SIDEBAR);
                    }
                } else {
                    Objective objective = cur.setObjective(null, p);
                    sb.addObjective(objective);
                    if (!sb.getObjective(DisplaySlots.SIDEBAR).isPresent()) {
                        sb.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
                    }
                }
            }
        }
    }


    @Override
    public void cancel() {
        running = false;
    }
}
