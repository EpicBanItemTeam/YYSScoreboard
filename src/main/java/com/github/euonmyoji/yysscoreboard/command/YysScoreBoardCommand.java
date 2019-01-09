package com.github.euonmyoji.yysscoreboard.command;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * @author yinyangshi
 */
public class YysScoreBoardCommand {
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
            .build();
}
