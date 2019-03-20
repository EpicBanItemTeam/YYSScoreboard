package com.github.euonmyoji.yysscoreboard.manager;

import com.github.euonmyoji.yysscoreboard.task.DisplayObjective;
import com.github.euonmyoji.yysscoreboard.task.DisplayTab;
import com.github.euonmyoji.yysscoreboard.task.IDisplayTask;
import com.github.euonmyoji.yysscoreboard.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.HashMap;

/**
 * @author yinyangshi
 */
public class TaskManager {
    private static HashMap<String, DisplayObjective> objectives = new HashMap<>();
    private static HashMap<String, DisplayTab> tabs = new HashMap<>();


    public static void registerTask(String id, DisplayObjective displayObjective) {

    }

    public static void registerTask(String id, DisplayTab displayObjective) {

    }

    public static void setupPlayer(Player p) {

    }

    public static void clear() {
        objectives.values().forEach(IDisplayTask::cancel);
        tabs.values().forEach(IDisplayTask::cancel);
        objectives.clear();
        tabs.clear();
    }

    public static void update() {
        Util.getStream(Sponge.getServer().getOnlinePlayers()).forEach(player -> {

        });
    }
}
