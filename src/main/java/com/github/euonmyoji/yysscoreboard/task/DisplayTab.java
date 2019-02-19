package com.github.euonmyoji.yysscoreboard.task;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.data.TabData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.List;

/**
 * @author yinyangshi
 */
public class DisplayTab implements Runnable {
    private final List<TabData> data;
    private int index = 0;
    private volatile boolean running = true;

    public DisplayTab(List<TabData> data) {
        this.data = data;
    }

    @Override
    public void run() {
        if (running) {
            if (running) {
                Task.Builder builder = Task.builder().execute(this);
                builder.delayTicks(data.get(index).delay);
                Sponge.getServer().getOnlinePlayers().forEach(data.get(index)::setTab);
                if (++index >= data.size()) {
                    index = 0;
                }
                if (PluginConfig.asyncUpdate) {
                    builder.async();
                }
                builder.submit(YysScoreBoard.plugin);
            }
        }
    }

    public void setPlayer(Player p) {
        int i = index;
        if (i >= data.size()) {
            i = 0;
        }
        data.get(i).setTab(p);
    }

    public void cancel() {
        running = false;
    }
}
