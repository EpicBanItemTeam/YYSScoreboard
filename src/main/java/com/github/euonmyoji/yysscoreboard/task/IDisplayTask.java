package com.github.euonmyoji.yysscoreboard.task;

import org.spongepowered.api.entity.living.player.Player;

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
     * @param p the player
     */
    void setupPlayer(Player p);
}
