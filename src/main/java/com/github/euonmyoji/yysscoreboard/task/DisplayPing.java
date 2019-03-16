package com.github.euonmyoji.yysscoreboard.task;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;

/**
 * @author yinyangshi
 */
public class DisplayPing implements Runnable {
    public static final String PING_OBJECTIVE_NAME = "yyssbPingObjective";
    private volatile boolean running = true;

    public DisplayPing() {
        Task.Builder builder = Task.builder().execute(this).delayTicks(PluginConfig.updateTick);
        if (PluginConfig.asyncTab) {
            builder.async();
        }
        builder.submit(YysScoreBoard.plugin);
    }

    @Override
    public void run() {
        if (running) {
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                Scoreboard sb = p.getScoreboard();
                Objective objective = sb.getObjective(PING_OBJECTIVE_NAME).orElse(null);
                if (objective == null) {
                    objective = Objective.builder()
                            .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                            .name(PING_OBJECTIVE_NAME)
                            .criterion(Criteria.DUMMY).build();

                    sb.addObjective(objective);
                }
                sb.updateDisplaySlot(objective, DisplaySlots.LIST);
                objective.getOrCreateScore(Text.of(p.getName())).setScore(p.getConnection().getLatency());
            }
            Task.Builder builder = Task.builder().execute(this).delayTicks(PluginConfig.updateTick);
            if (PluginConfig.asyncTab) {
                builder.async();
            }
            builder.submit(YysScoreBoard.plugin);
        }
    }

    public void cancel() {
        running = false;
    }
}
