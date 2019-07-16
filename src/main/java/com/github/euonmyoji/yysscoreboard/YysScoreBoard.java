package com.github.euonmyoji.yysscoreboard;

import com.github.euonmyoji.yysscoreboard.command.YysScoreBoardCommand;
import com.github.euonmyoji.yysscoreboard.configuration.GlobalPlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig;
import com.github.euonmyoji.yysscoreboard.manager.PlaceHolderManager;
import com.github.euonmyoji.yysscoreboard.manager.TaskManager;
import com.github.euonmyoji.yysscoreboard.manager.TextManager;
import com.github.euonmyoji.yysscoreboard.manager.TextManagerImpl;
import com.github.euonmyoji.yysscoreboard.task.DisplayNumber;
import com.google.inject.Inject;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
//https://api.github.com/repos/euOnmyoji/YYSScoreboard/releases

/**
 * @author yinyangshi
 */
@Plugin(id = "yysscoreboard", name = "YYS ScoreBoard", version = YysScoreBoard.VERSION,
        authors = "yinyangshi", description = "Scoreboard for special require",
        dependencies = {@Dependency(id = YysScoreBoard.PAPI_ID, optional = true)})
public class YysScoreBoard {
    public static final String VERSION = "@spongeVersion@";
    static final String PAPI_ID = "placeholderapi";
    public static TextManager textManager;
    public static Logger logger;
    public static YysScoreBoard plugin;
    private final Metrics2 metrics;
    private boolean enabledPlaceHolderApi = false;

    @Inject
    public YysScoreBoard(@ConfigDir(sharedRoot = false) Path cfgDir, Logger logger, Metrics2 metrics) {
        plugin = this;
        PluginConfig.defaultCfgDir = cfgDir;
        try {
            Files.createDirectories(cfgDir);
        } catch (IOException e) {
            logger.warn("create dir failed", e);
        }

        YysScoreBoard.logger = logger;
        this.metrics = metrics;
    }


    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        PluginConfig.init();
        GlobalPlayerConfig.init();
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        //code is copy and paste
        //so there is no ifPresnet
        Scoreboard scoreboard = Sponge.getServer().getServerScoreboard().orElse(null);
        if (scoreboard != null) {
            scoreboard.getObjectives().stream()
                    .filter(objective -> objective.getName().startsWith(ScoreBoardConfig.OBJECTIVE_PREFIX))
                    .forEach(objective -> ScoreBoardConfig.getStaticScoreBoard().removeObjective(objective));
            scoreboard.getObjective(DisplayNumber.PING_OBJECTIVE_NAME).ifPresent(scoreboard::removeObjective);
        }
        ScoreBoardConfig.init();
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, YysScoreBoardCommand.YYSSB, "yyssb", "yysscoreboard", "sbyys");
        hook();
        try {
            if (!Sponge.getMetricsConfigManager().areMetricsEnabled(this)) {
                Sponge.getServer().getConsole()
                        .sendMessage(Text.of("[YYSSB]If you think YYSSB is a good SB and want to support SB, please enable metrics, thanks!"));
            }
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            //do not spam the server (ignore)
            metrics.cancel();
            Task.builder().delayTicks(60 * 20).execute(metrics::cancel).submit(this);
            logger.debug("NoMetricsManagerClassDefFound, try canceling the metrics");
        }
    }

    @Listener(order = Order.LATE)
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player p = event.getTargetEntity();
        UUID uuid = p.getUniqueId();
        try {
            PlayerConfig pc = PlayerConfig.of(uuid);
            pc.init();
            TaskManager.update(p, pc);
        } catch (IOException e) {
            logger.warn("load failed", e);
        }
    }

    @Listener
    public void onStopping(GameStoppingServerEvent event) {
        ScoreBoardConfig.getStaticScoreBoard().getObjectives().stream()
                .filter(objective -> objective.getName().startsWith(ScoreBoardConfig.OBJECTIVE_PREFIX))
                .forEach(objective -> ScoreBoardConfig.getStaticScoreBoard().removeObjective(objective));

        Sponge.getServer().getServerScoreboard().ifPresent(scoreboard -> scoreboard.getObjectives().stream()
                .filter(objective -> objective.getName().startsWith(ScoreBoardConfig.OBJECTIVE_PREFIX))
                .forEach(objective -> ScoreBoardConfig.getStaticScoreBoard().removeObjective(objective)));
        Sponge.getServer().getOnlinePlayers().stream().map(Player::getScoreboard)
                .forEach(scoreboard -> {
                    scoreboard.getObjectives().stream()
                            .filter(objective -> objective.getName().startsWith(ScoreBoardConfig.OBJECTIVE_PREFIX))
                            .forEach(objective -> ScoreBoardConfig.getStaticScoreBoard().removeObjective(objective));
                    scoreboard.getObjective(DisplayNumber.PING_OBJECTIVE_NAME).ifPresent(scoreboard::removeObjective);
                });
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        Player p = event.getTargetEntity();
        Scoreboard scoreboard = p.getScoreboard();
        scoreboard.getObjectives().stream()
                .filter(objective -> objective.getName().startsWith(ScoreBoardConfig.OBJECTIVE_PREFIX))
                .forEach(objective -> ScoreBoardConfig.getStaticScoreBoard().removeObjective(objective));
        scoreboard.getObjective(DisplayNumber.PING_OBJECTIVE_NAME).ifPresent(scoreboard::removeObjective);
    }


    public void reload() {
        PluginConfig.reload();
        ScoreBoardConfig.reload();
        GlobalPlayerConfig.reload();

        TaskManager.update();
    }

    private void hook() {

        //hook PAPI
        if (Sponge.getPluginManager().getPlugin(PAPI_ID).isPresent()) {
            if (!enabledPlaceHolderApi) {
                logger.info("hooked PAPI");
            }
            enabledPlaceHolderApi = true;
            textManager = PlaceHolderManager.getInstance();

        } else {
            textManager = TextManagerImpl.getInstance();
        }
    }
}
