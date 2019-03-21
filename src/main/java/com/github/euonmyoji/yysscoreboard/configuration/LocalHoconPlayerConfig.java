package com.github.euonmyoji.yysscoreboard.configuration;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class LocalHoconPlayerConfig implements PlayerConfig {
    private final UUID uuid;

    public LocalHoconPlayerConfig(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getDisplayObjectiveID() {
        return null;
    }

    @Override
    public void setDisplayObjectiveID(String id) {

    }

    @Override
    public String getDisplayTabID() {
        return null;
    }

    @Override
    public void setDisplayTabID(String id) {

    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    public void save() {

    }
}
