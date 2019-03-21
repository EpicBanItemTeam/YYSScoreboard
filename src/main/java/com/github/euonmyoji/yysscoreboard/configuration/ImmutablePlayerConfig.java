package com.github.euonmyoji.yysscoreboard.configuration;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class ImmutablePlayerConfig implements PlayerConfig {
    private final String objectiveID;
    private final String tabID;
    private final UUID uuid;

    public ImmutablePlayerConfig(String objectiveID, String tabID, UUID uuid) {
        this.objectiveID = objectiveID;
        this.tabID = tabID;
        this.uuid = uuid;
    }

    @Override
    public String getDisplayObjectiveID() {
        return this.objectiveID;
    }

    @Override
    public String getDisplayTabID() {
        return this.tabID;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void setDisplayObjectiveID(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDisplayTabID(String id) {
        throw new UnsupportedOperationException();

    }

}