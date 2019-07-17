package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.util.UUID;

/**
 * @author yinyangshi
 */
public class LocalHoconPlayerConfig implements PlayerConfig {
    private final UUID uuid;
    private final HoconConfigurationLoader loader;
    private final CommentedConfigurationNode cfg;

    LocalHoconPlayerConfig(UUID uuid) throws IOException {
        this.uuid = uuid;
        loader = HoconConfigurationLoader.builder().setPath(PluginConfig.cfgDir.resolve("PlayerData").resolve(uuid + ".conf")).build();
        cfg = loader.load();
    }

    @Override
    public void init() {
        try {
            check();
        } catch (IOException e) {
            YysScoreBoard.logger.warn("ioe", e);
        }
    }

    @Override
    public String getDisplayObjectiveID() {
        return cfg.getNode("use-id", "objective").getString("main");
    }

    @Override
    public void setDisplayObjectiveID(String id) throws IOException {
        cfg.getNode("use-id", "objective").setValue(id);
        save();
    }

    @Override
    public String getDisplayTabID() {
        return cfg.getNode("use-id", "tab").getString("main");

    }

    @Override
    public void setDisplayTabID(String id) throws IOException {
        cfg.getNode("use-id", "tab").setValue(id);
        save();
    }

    @Override
    public boolean isToggle() {
        return cfg.getNode("toggle").getBoolean(true);
    }

    @Override
    public void setToggle(boolean toggle) throws IOException {
        cfg.getNode("toggle").setValue(toggle);
        save();
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void save() throws IOException {
        loader.save(cfg);
    }
}
