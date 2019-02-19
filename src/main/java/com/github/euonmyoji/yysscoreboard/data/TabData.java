package com.github.euonmyoji.yysscoreboard.data;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;

import static com.github.euonmyoji.yysscoreboard.YysScoreBoard.textManager;

/**
 * @author yinyangshi
 */
public class TabData {
    public final int delay;
    private final String header;
    private final String footer;

    public TabData(CommentedConfigurationNode node, int defDelay) {
        header = node.getNode("header").getString("");
        footer = node.getNode("footer").getString("");
        this.delay = node.getNode("delay").getInt(defDelay);
    }

    public void setTab(Player p) {
        p.getTabList().setHeaderAndFooter(textManager.toText(header, p), textManager.toText(footer, p));
    }
}
