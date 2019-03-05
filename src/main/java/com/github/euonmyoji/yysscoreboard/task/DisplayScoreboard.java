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
public class DisplayScoreboard implements Runnable {
    private final List<ObjectiveData> data;
    private int index = 0;
    private volatile boolean running = true;

    public DisplayScoreboard(List<ObjectiveData> data) {
        this.data = data;
    }

    @Override
    public void run() {
        if (running) {
            setPlayerScoreBoard(Sponge.getServer().getOnlinePlayers());
            Task.Builder builder = Task.builder().execute(this);
            if (PluginConfig.asyncUpdate) {
                builder.async();
            }
            builder.delayTicks(data.get(index).delay);
            if (++index >= data.size()) {
                index = 0;
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

    public synchronized void setScoreBoard(Scoreboard sb, Player p) {
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

    public void cancel() {
        running = false;
    }
}
