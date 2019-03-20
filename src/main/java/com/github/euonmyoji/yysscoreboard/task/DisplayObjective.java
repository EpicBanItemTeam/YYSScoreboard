package com.github.euonmyoji.yysscoreboard.task;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.data.ObjectiveData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.util.Collection;
import java.util.List;

import static com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig.*;

/**
 * @author yinyangshi
 */
public class DisplayObjective implements IDisplayTask {
    private final List<ObjectiveData> data;
    private int index = 0;
    private volatile boolean running;

    public DisplayObjective(List<ObjectiveData> data) {
        this.data = data;
        running = !data.isEmpty();
    }

    @Override
    public void run() {
        if (running) {
            Task.Builder builder = Task.builder().execute(this);
            try {
                setPlayerScoreBoard(Sponge.getServer().getOnlinePlayers());
                if (PluginConfig.asyncSidebar) {
                    builder.async();
                }
                builder.delayTicks(data.get(index).delay);
                if (++index >= data.size()) {
                    index = 0;
                }
            } catch (Throwable e) {
                YysScoreBoard.logger.warn("something wrong", e);
            }
            builder.submit(YysScoreBoard.plugin);
        }
    }

    private void setPlayerScoreBoard(Collection<Player> players) {
        boolean setStatic = false;
        Scoreboard sb;
        for (Player p : players) {
            if (PlayerConfig.list.contains(p.getUniqueId())) {
                sb = p.getScoreboard();
                sb.getObjective(OBJECTIVE_NAME).ifPresent(sb::removeObjective);
            } else {
                sb = getPlayerScoreboard(p);

                if (sb != p.getScoreboard()) {
                    p.setScoreboard(sb);
                }

                if (sb == getStaticScoreBoard()) {
                    if (!setStatic) {
                        setStatic = true;
                        update(p);
                    }
                } else {
                    update(p);
                }
            }
        }
    }

    @Override
    public void setupPlayer(Player p) {
        if (running) {
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

            update(p);
        }
    }

    private void update(Player p) {
        Scoreboard sb = p.getScoreboard();
        Objective objective = sb.getObjective(OBJECTIVE_NAME).orElse(null);
        boolean shouldAdd = false;
        if (objective == null) {
            shouldAdd = true;
        }
        objective = data.get(index).setObjective(objective, p);
        if (shouldAdd) {
            sb.addObjective(objective);
        }
        sb.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);
    }

    @Override
    public void cancel() {
        running = false;
    }
}
