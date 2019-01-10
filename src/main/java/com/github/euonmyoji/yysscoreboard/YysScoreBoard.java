package com.github.euonmyoji.yysscoreboard;

import com.github.euonmyoji.yysscoreboard.command.YysScoreBoardCommand;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig;
import com.github.euonmyoji.yysscoreboard.manager.PlaceHolderManager;
import com.github.euonmyoji.yysscoreboard.manager.TextManager;
import com.github.euonmyoji.yysscoreboard.manager.TextManagerImpl;
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
    static final String PAPI_ID = "placeholderapi";
    public static TextManager textManager;
    public static Logger logger;
    public static YysScoreBoard plugin;
    public static final String VERSION = "@spongeVersion@";
    @Inject
    @ConfigDir(sharedRoot = false)
    public Path cfgDir;
    private boolean enabledPlaceHolderAPI = false;
    @Inject
    private Metrics2 metrics;


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
        } catch (IOException e) {
            logger.warn("init plugin IOE!", e);
        }
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        ScoreBoardConfig.init();
    }

    private Task updateTask;

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        Sponge.getCommandManager().register(this, YysScoreBoardCommand.YYSSB, "yyssb", "yysscoreboard");
        updateTaskSubmit();
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
        ScoreBoardConfig.setPlayerScoreBoard(p);
    }

    @Listener
    public void onStopping(GameStoppingServerEvent event) {
        if (PluginConfig.isStaticMode) {
            ScoreBoardConfig.getStaticScoreBoard().getObjective(ScoreBoardConfig.NAME)
                    .ifPresent(objective -> ScoreBoardConfig.getStaticScoreBoard().removeObjective(objective));
        }
        if (!PluginConfig.isStableMode) {
            Sponge.getServer().getOnlinePlayers().stream().map(Player::getScoreboard)
                    .forEach(scoreboard -> scoreboard.getObjective(ScoreBoardConfig.NAME)
                            .ifPresent(scoreboard::removeObjective));
        }
    }


    public void reload() {
        PluginConfig.reload();
        ScoreBoardConfig.reload();
        Sponge.getServer().getOnlinePlayers().forEach(ScoreBoardConfig::setPlayerScoreBoard);
        if (updateTask != null) {
            updateTask.cancel();
        }
        updateTaskSubmit();
    }

    private void updateTaskSubmit() {
        Task.Builder builder = Task.builder();
        if (PluginConfig.asyncUpdate) {
            builder.async();
        }
        if (PluginConfig.updateTick > 0) {
            builder.intervalTicks(PluginConfig.updateTick);
        }
        updateTask = builder.name("YYSScoreboard - update score board").execute(() -> Sponge.getServer().getOnlinePlayers()
                .forEach(ScoreBoardConfig::setPlayerScoreBoard)).async().submit(this);
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
