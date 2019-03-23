package com.github.euonmyoji.yysscoreboard.task;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

/**
 * @author yinyangshi
 */
public interface IDisplayTask extends Runnable {

    /**
     * 取消任务
     */
    void cancel();

    /**
     * 设置玩家
     *
     * @param p the player
     */
    void setupPlayer(Player p);

    /**
     * 开始执行任务
     */
    default void start() {
        if (PluginConfig.asyncDefault) {
            Task.builder().async().execute(this).submit(YysScoreBoard.plugin);
        } else {
            Task.builder().execute(this).submit(YysScoreBoard.plugin);
        }
    }
}
