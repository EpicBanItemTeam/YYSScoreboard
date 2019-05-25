package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.manager.TaskManager;

import java.io.IOException;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public interface PlayerConfig {
    /**
     * 获得某个玩家的配置
     *
     * @param uuid the uuid of the player
     * @return the config of the player
     * @throws IOException if load failed
     */
    static PlayerConfig of(UUID uuid) throws IOException {
        return new LocalHoconPlayerConfig(uuid);
    }

    /**
     * 初始化玩家配置数据
     */
    void init();

    /**
     * 获得默认显示的objectiveID
     *
     * @return objective ID
     */
    String getDisplayObjectiveID();

    /**
     * 设置display的objective ID
     *
     * @param id the id of objective id
     * @throws IOException save failed
     */
    void setDisplayObjectiveID(String id) throws IOException;

    /**
     * 玩家默认显示的tab ID
     *
     * @return the id of tab id
     */
    String getDisplayTabID();

    /**
     * 设置玩家显示的tab id
     *
     * @param id the id of tab ID
     * @throws IOException save failed
     */
    void setDisplayTabID(String id) throws IOException;

    /**
     * Is toggle using id by config
     *
     * @return true if toggle
     */
    boolean isToggle();

    /**
     * Set whether toggle by config or not
     *
     * @param toggle true if toggle
     * @throws IOException save failed
     */
    void setToggle(boolean toggle) throws IOException;

    /**
     * get the uuid of the player
     *
     * @return the uuid of the player
     */
    UUID getUUID();

    /**
     * 检查使用的id是否存在
     *
     * @throws IOException if any exception
     */
    default void check() throws IOException {
        if (!TaskManager.objectives.containsKey(getDisplayObjectiveID())) {
            setDisplayObjectiveID(TaskManager.objectives.containsKey("main") ? "main" : TaskManager.objectives.keySet().stream().findAny().orElse(""));
        }

        if (!TaskManager.tabs.containsKey(getDisplayTabID())) {
            setDisplayTabID(TaskManager.tabs.containsKey("main") ? "main" : TaskManager.tabs.keySet().stream().findAny().orElse(""));
        }
    }

    /**
     * 保存玩家数据
     *
     * @throws Exception if something happened
     */
    void save() throws Exception;
}
