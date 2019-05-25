package com.github.euonmyoji.yysscoreboard.command;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.GlobalPlayerConfig;
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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.spongepowered.api.command.args.GenericArguments.*;

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
                        if (GlobalPlayerConfig.list.contains(uuid)) {
                            src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.off.already")));
                            Scoreboard sb = ((Player) src).getScoreboard();
                            if (PluginConfig.isStaticMode || PluginConfig.isStableMode) {
                                ((Player) src).setScoreboard(Scoreboard.builder().build());
                            } else {
                                sb.getObjective(ScoreBoardConfig.getObjectiveName(((Player) src))).ifPresent(sb::removeObjective);
                            }
                        } else {
                            GlobalPlayerConfig.list.add(uuid);
                            Scoreboard sb = ((Player) src).getScoreboard();
                            if (PluginConfig.isStaticMode || PluginConfig.isStableMode) {
                                ((Player) src).setScoreboard(Scoreboard.builder().build());
                            } else {
                                sb.getObjective(ScoreBoardConfig.getObjectiveName(((Player) src))).ifPresent(sb::removeObjective);
                            }

                            try {
                                GlobalPlayerConfig.saveList();
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
                    if (GlobalPlayerConfig.list.contains(((Player) src).getUniqueId())) {
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
                        if (GlobalPlayerConfig.list.contains(uuid)) {
                            GlobalPlayerConfig.list.remove(uuid);
                            TaskManager.setupPlayer(((Player) src));
                            try {
                                GlobalPlayerConfig.saveList();
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

    private static final CommandSpec USE = CommandSpec.builder()
            .permission("yysscoreboard.command.use")
            .arguments(onlyOne(userOrSource(Text.of("user"))),
                    onlyOne(string(Text.of("id"))),
                    flags().flag("-tab", "-t").buildWith(none()),
                    flags().flag("-sb", "-scoreboard", "-objective").buildWith(none()))
            .executor((src, args) -> {
                User user = args.<User>getOne("user").orElseThrow(NoSuchElementException::new);
                final String adminPermission = "yysscoreboard.admin.command.use";
                if (src instanceof Identifiable && !((Identifiable) src).getUniqueId().equals(user.getUniqueId())
                        && !src.hasPermission(adminPermission)) {
                    src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.use.noPermission")));

                } else {
                    try {
                        PlayerConfig pc = PlayerConfig.of(user.getUniqueId());
                        boolean tab = args.hasAny("tab");
                        boolean sb = args.hasAny("sb");
                        String id = args.<String>getOne("id").orElseThrow(NoSuchElementException::new);
                        if (!tab && !sb) {
                            pc.setDisplayObjectiveID(id);
                            pc.setDisplayTabID(id);
                        } else {
                            if (tab && TaskManager.tabs.containsKey(id)) {
                                pc.setDisplayTabID(id);
                            }
                            if (sb && TaskManager.objectives.containsKey(id)) {
                                pc.setDisplayObjectiveID(id);
                            }
                        }
                        if (GlobalPlayerConfig.list.remove(user.getUniqueId())) {
                            try {
                                GlobalPlayerConfig.saveList();
                            } catch (ObjectMappingException e) {
                                YysScoreBoard.logger.warn("wtf", e);

                            }
                        }
                        src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.use.successful")));
                        user.getPlayer().ifPresent(player -> TaskManager.update(player, pc));
                        return CommandResult.success();
                    } catch (IOException e) {
                        src.sendMessage(Util.toText(LanguageManager.getString("yysscoreboard.command.use.exception")));
                        YysScoreBoard.logger.warn("load player config failed", e);
                    }
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
            .child(USE, "use")
            .build();
}
