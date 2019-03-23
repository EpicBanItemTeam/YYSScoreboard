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
    private boolean enabledPlaceHolderAPI = false;
    private Metrics2 metrics;

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
        ScoreBoardConfig.getStaticScoreBoard().getObjective(ScoreBoardConfig.OBJECTIVE_NAME)
                .ifPresent(objective -> {
                    Scoreboard sb = ScoreBoardConfig.getStaticScoreBoard();
                    sb.removeObjective(objective);
                    sb.getObjective(DisplayNumber.PING_OBJECTIVE_NAME).ifPresent(sb::removeObjective);

                });

        Sponge.getServer().getOnlinePlayers().stream().map(Player::getScoreboard)
                .forEach(scoreboard -> {
                    scoreboard.getObjective(ScoreBoardConfig.OBJECTIVE_NAME)
                            .ifPresent(scoreboard::removeObjective);
                    scoreboard.getObjective(DisplayNumber.PING_OBJECTIVE_NAME).ifPresent(scoreboard::removeObjective);
                });
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
