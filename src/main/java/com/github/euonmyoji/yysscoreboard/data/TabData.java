package com.github.euonmyoji.yysscoreboard.data;

import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.util.RandomDelay;
import com.github.euonmyoji.yysscoreboard.util.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.text.Text;

import static com.github.euonmyoji.yysscoreboard.YysScoreBoard.textManager;

/**
 * @author yinyangshi
 */
public class TabData {
    public final RandomDelay delay;
    private final String header;
    private final String footer;
    private final String prefix;
    private final String suffix;

    public TabData(CommentedConfigurationNode node, int defDelay) {
        header = node.getNode("header").getString();
        footer = node.getNode("footer").getString();
        prefix = node.getNode("prefix").getString();
        suffix = node.getNode("suffix").getString();
        this.delay = new RandomDelay(node.getNode("delay").getString(defDelay + ""));
    }

    public void setTab(Player p) {
        TabList tabList = p.getTabList();
        if (header != null) {
            tabList.setHeader(textManager.toText(header, p));
        }
        if (footer != null) {
            tabList.setFooter(textManager.toText(footer, p));
        }
        if (prefix != null || suffix != null) {
            Text prefix = textManager.toText(this.prefix, p);
            Text suffix = textManager.toText(this.suffix, p);
            Text displayName = prefix.toBuilder()
                    .append(p.getDisplayNameData().displayName().get())
                    .append(suffix)
                    .build();
            Sponge.getServer().getOnlinePlayers().forEach(player -> player
                    .getTabList().getEntry(p.getUniqueId()).ifPresent(entry -> {
                        if (p == player) {
                            entry.setDisplayName(prefix.toBuilder()
                                    .append(Util.toText(PluginConfig.getSelfNamePrefix() + p.getName()))
                                    .append(suffix)
                                    .build());
                        } else {
                            entry.setDisplayName(displayName);
                        }
                    }));
        }
    }
}
