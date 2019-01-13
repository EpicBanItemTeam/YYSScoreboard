package com.github.euonmyoji.yysscoreboard.command;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PlayerConfig;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.configuration.ScoreBoardConfig;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
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
                            src.sendMessage(Text.of("[YYSSB]你已经关闭scoreboard了"));
                            Scoreboard sb = ((Player) src).getScoreboard();
                            sb.getObjective(ScoreBoardConfig.NAME).ifPresent(sb::removeObjective);
                        } else {
                            PlayerConfig.list.add(uuid);
                            Scoreboard sb = ((Player) src).getScoreboard();
                            sb.getObjective(ScoreBoardConfig.NAME).ifPresent(sb::removeObjective);
                            try {
                                PlayerConfig.saveList();
                                src.sendMessage(Text.of("[YYSSB]关闭scoreboard成功"));
                            } catch (ObjectMappingException e) {
                                YysScoreBoard.logger.warn("error while setting off", e);
                                src.sendMessage(Text.of("[YYSSB]配置文件错误!"));
                            }
                        }
                    };
                    if (PluginConfig.asyncUpdate) {
                        Task.builder().async().execute(r).submit(YysScoreBoard.plugin);
                    } else {
                        r.run();
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
                            ScoreBoardConfig.setPlayerScoreBoard(((Player) src));
                            try {
                                PlayerConfig.saveList();
                                src.sendMessage(Text.of("[YYSSB]开启scoreboard成功"));
                            } catch (ObjectMappingException e) {
                                YysScoreBoard.logger.warn("error while setting on", e);
                                src.sendMessage(Text.of("[YYSSB]配置文件错误!"));
                            }
                        } else {
                            src.sendMessage(Text.of("[YYSSB]你已经开启scoreboard了"));
                        }
                    };
                    if (PluginConfig.asyncUpdate) {
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
            .permission("yysscoreboard.command.admin.reload")
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
            .build();
}
