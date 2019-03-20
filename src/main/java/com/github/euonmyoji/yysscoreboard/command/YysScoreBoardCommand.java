package com.github.euonmyoji.yysscoreboard.command;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig;
import com.github.euonmyoji.yysscoreboard.manager.LanguageManager;
import com.github.euonmyoji.yysscoreboard.manager.TaskManager;
import com.github.euonmyoji.yysscoreboard.util.Util;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class YysScoreBoardCommand {
    private static final CommandSpec OFF = CommandSpec.builder()
            .permission("yysscoreboard.command.off")
            .executor((src, args) -> {
                if (src instanceof Player) {
                    Runnable r = () -> {
                        UUID uuid = ((Player) src).getUniqueId();
                        if (PlayerConfig.list.contains(uuid)) {
                            src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.off.already")));
                            Scoreboard sb = ((Player) src).getScoreboard();
                            sb.getObjective(ScoreBoardConfig.OBJECTIVE_NAME).ifPresent(sb::removeObjective);
                        } else {
                            PlayerConfig.list.add(uuid);
                            Scoreboard sb = ((Player) src).getScoreboard();
                            sb.getObjective(ScoreBoardConfig.OBJECTIVE_NAME).ifPresent(sb::removeObjective);
                            try {
                                PlayerConfig.saveList();
                                src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.off.successful")));
                            } catch (ObjectMappingException e) {
                                YysScoreBoard.logger.warn("error while setting off", e);
                                src.sendMessage(Text.of("[YYSSB]ERROR:" + e.getMessage()));
                            }
                        }
                    };
                    if (PluginConfig.asyncDefault) {
                        Task.builder().async().execute(r).submit(YysScoreBoard.plugin);
                    } else {
                        r.run();
                    }
                    return CommandResult.success();
                }
                return CommandResult.empty();
            })
            .build();

    private static final CommandSpec TOGGLE = CommandSpec.builder()
            .executor((src, args) -> {
                if (src instanceof Player) {
                    if (PlayerConfig.list.contains(((Player) src).getUniqueId())) {
                        Sponge.getCommandManager().process(src, "yyssb on");
                    } else {
                        Sponge.getCommandManager().process(src, "yyssb off");
                    }
                    return CommandResult.success();
                }
                return CommandResult.empty();
            })
            .build();

    private static final CommandSpec ON = CommandSpec.builder()
            .permission("yysscoreboard.command.on")
            .executor((src, args) -> {
                if (src instanceof Player) {
                    Runnable r = () -> {
                        UUID uuid = ((Player) src).getUniqueId();
                        if (PlayerConfig.list.contains(uuid)) {
                            PlayerConfig.list.remove(uuid);
                            TaskManager.setupPlayer(((Player) src));
                            try {
                                PlayerConfig.saveList();
                                src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.on.successful")));
                            } catch (ObjectMappingException e) {
                                YysScoreBoard.logger.warn("error while setting on", e);
                                src.sendMessage(Text.of("[YYSSB]ERROR:" + e.getMessage()));
                            }
                        } else {
                            src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.on.already")));
                            TaskManager.setupPlayer(((Player) src));
                        }
                    };
                    if (PluginConfig.asyncDefault) {
                        Task.builder().async().execute(r).submit(YysScoreBoard.plugin);
                    } else {
                        r.run();
                    }
                    return CommandResult.success();
                }
                return CommandResult.empty();
            })
            .build();

    private static final CommandSpec RELOAD = CommandSpec.builder()
            .permission("yysscoreboard.admin.command.reload")
            .executor((src, args) -> {
                src.sendMessage(Text.of("[YYSSB]start reloading"));
                long start = System.currentTimeMillis();
                YysScoreBoard.plugin.reload();
                long cost = System.currentTimeMillis() - start;
                src.sendMessage(Text.of("[YYSSB]reloaded successful in " + cost + " ms"));
                return CommandResult.success();
            })
            .build();

    public static final CommandSpec YYSSB = CommandSpec.builder()
            .permission("yysscoreboard.command.yyssb")
            .executor((src, args) -> {
                src.sendMessage(Text.of("YYS Scoreboard version:" + YysScoreBoard.VERSION));
                return CommandResult.success();
            })
            .child(RELOAD, "reload")
            .child(ON, "on")
            .child(OFF, "off")
            .child(TOGGLE, "toggle")
            .build();
}
