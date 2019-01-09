package com.github.euonmyoji.yysscoreboard.manager;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

/**
 * @author yinyangshi
 */
public interface TextManager {
    /**
     * string转text
     *
     * @param s 文本
     * @param p 可能的玩家
     * @return text
     */
    Text toText(String s, @Nullable Player p);
}


