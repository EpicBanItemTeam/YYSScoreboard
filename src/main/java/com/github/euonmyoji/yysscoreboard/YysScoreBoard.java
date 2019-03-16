package com.github.euonmyoji.yysscoreboard;

import com.github.euonmyoji.yysscoreboard.command.YysScoreBoardCommand;
import com.github.euonmyoji.yysscoreboard.configuration.PlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig;
import com.github.euonmyoji.yysscoreboard.manager.PlaceHolderManager;
import com.github.euonmyoji.yysscoreboard.manager.TextManager;
import com.github.euonmyoji.yysscoreboard.manager.TextManagerImpl;
import com.github.euonmyoji.yysscoreboard.task.DisplayObjective;
import com.github.euonmyoji.yysscoreboard.task.DisplayPing;
import com.github.euonmyoji.yysscoreboard.task.DisplayTab;
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
    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;
    private boolean enabledPlaceHolderAPI = false;
    @Inject
    private Metrics2 metrics;
    private DisplayObjective displayTask;
    private DisplayTab displayTab;
    private DisplayPing displayPing;

    @Inject
    public void setLogger(Logger l) {
        logger = l;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;
        try {
            Files.createDirectories(cfgDir);
            PluginConfig.init();
            PlayerConfig.init();
        } catch (IOException e) {
            logger.warn("init plugin IOE!", e);
        }
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
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
            logger.info("NoMetricsManagerClassDefFound, try canceling the metrics");
        }
    }

    @Listener(order = Order.LATE)
    public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
        Player p = event.getTargetEntity();
        ScoreBoardConfig.setPlayerScoreboard(p);
        displayTask.setScoreBoard(p.getScoreboard(), p);
        displayTab.setPlayer(p);
    }

    @Listener
    public void onStopping(GameStoppingServerEvent event) {
        ScoreBoardConfig.getStaticScoreBoard().getObjective(ScoreBoardConfig.OBJECTIVE_NAME)
                .ifPresent(objective -> {
                    Scoreboard sb = ScoreBoardConfig.getStaticScoreBoard();
                    sb.removeObjective(objective);
                    sb.getObjective(DisplayPing.PING_OBJECTIVE_NAME).ifPresent(sb::removeObjective);

                });

        Sponge.getServer().getOnlinePlayers().stream().map(Player::getScoreboard)
                .forEach(scoreboard -> {
                    scoreboard.getObjective(ScoreBoardConfig.OBJECTIVE_NAME)
                            .ifPresent(scoreboard::removeObjective);
                    scoreboard.getObjective(DisplayPing.PING_OBJECTIVE_NAME).ifPresent(scoreboard::removeObjective);
                });
    }


    public void reload() {
        if (displayTask != null) {
            displayTask.cancel();
        }
        if (displayTab != null) {
            displayTab.cancel();
        }
        if (displayPing != null) {
            displayPing.cancel();
        }
        PluginConfig.reload();
        ScoreBoardConfig.reload();
        PlayerConfig.reload();
        if (PluginConfig.showPing) {
            displayPing = new DisplayPing();
        }
        Sponge.getServer().getOnlinePlayers().forEach(p -> {
            ScoreBoardConfig.setPlayerScoreboard(p);
            displayTask.setScoreBoard(p.getScoreboard(), p);
            displayTab.setPlayer(p);
        });
    }

    public void setDisplayTask(DisplayObjective displayTask) {
        if (this.displayTask != null) {
            this.displayTask.cancel();
        }
        this.displayTask = displayTask;
        displayTask.run();
    }

    public void setDisplayTab(DisplayTab displayTab) {
        if (this.displayTab != null) {
            this.displayTab.cancel();
        }
        this.displayTab = displayTab;
        displayTab.run();
    }

    public void setDisplayPing(DisplayPing task) {
        if (this.displayPing != null) {
            this.displayPing.cancel();
        }
        this.displayPing = task;
    }

    private void hook() {

        //hook PAPI
        if (Sponge.getPluginManager().getPlugin(PAPI_ID).isPresent()) {
            if (!enabledPlaceHolderAPI) {
                logger.info("hooked PAPI");
            }
            enabledPlaceHolderAPI = true;
            textManager = PlaceHolderManager.getInstance();

        } else {
            textManager = TextManagerImpl.getInstance();
        }
    }
}
