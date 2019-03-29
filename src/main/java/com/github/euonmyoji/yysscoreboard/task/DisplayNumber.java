package com.github.euonmyoji.yysscoreboard.task;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.manager.PlaceHolderManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;

import java.util.NoSuchElementException;

/**
 * @author yinyangshi
 */
public class DisplayNumber implements Runnable {
    public static final String PING_OBJECTIVE_NAME = "yyssbPing";
    private static Objective pingObjective;
    private final NumberSupplier supplier;

    private volatile boolean running = true;

    public DisplayNumber(NumberSupplier supplier) {
        this.supplier = supplier;
        Task.Builder builder = Task.builder().execute(this).delayTicks(PluginConfig.updateTick);
        if (PluginConfig.asyncTab) {
            builder.async();
        }
        builder.submit(YysScoreBoard.plugin);
    }

    private static Objective getPingObjective() {
        if (pingObjective == null) {
            pingObjective = Objective.builder()
                    .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                    .name(PING_OBJECTIVE_NAME)
                    .displayName(Text.of("ping"))
                    .criterion(Criteria.DUMMY)
                    .build();
        }
        return pingObjective;
    }

    @Override
    public void run() {
        if (running) {
            try {
                for (Player p : Sponge.getServer().getOnlinePlayers()) {
                    Scoreboard sb = p.getScoreboard();
                    Objective objective = sb.getObjective(PING_OBJECTIVE_NAME).orElse(null);
                    if (objective != null && objective != getPingObjective()) {
                        sb.removeObjective(objective);
                        objective = null;
                    }
                    if (objective == null) {
                        objective = getPingObjective();
                        sb.addObjective(objective);
                    }
                    sb.updateDisplaySlot(objective, DisplaySlots.LIST);
                    objective.getOrCreateScore(Text.of(p.getName())).setScore(supplier.getInt(p));
                }
            } catch (IllegalArgumentException e) {
                YysScoreBoard.logger.warn("something argument wrong", e);
                cancel();
                return;
            } catch (Throwable e) {
                YysScoreBoard.logger.warn("something wrong", e);
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

    @FunctionalInterface
    public interface NumberSupplier {
        NumberSupplier PING = p -> p.getConnection().getLatency();
        NumberSupplier HEALTHY = p -> p.get(Keys.HEALTH).orElseThrow(NoSuchElementException::new).intValue();

        /**
         * 获得int 用于tab的score
         *
         * @param p 玩家
         * @return the int of the result
         */
        int getInt(Player p);

        class Default implements NumberSupplier {
            private final String s;

            public Default(String s) {
                this.s = s;
                if (PlaceHolderManager.getInstance().service == null) {
                    throw new RuntimeException("PAPI not supported");
                }
            }

            @Override
            public int getInt(Player p) {
                Object object = PlaceHolderManager.getInstance().service.parse(s, p, null);
                if (object instanceof Number) {
                    return ((Number) object).intValue();
                }

                if (object instanceof String) {
                    try {
                        return Integer.parseInt(((String) object), 10);
                    } catch (NumberFormatException e) {
                        return (int) Double.parseDouble(((String) object));
                    }
                }
                throw new IllegalArgumentException("The string can't be parsed, input:" + s);
            }
        }
    }
}
