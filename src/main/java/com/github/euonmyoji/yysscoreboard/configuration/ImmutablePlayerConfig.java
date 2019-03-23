package com.github.euonmyoji.yysscoreboard.configuration;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public class ImmutablePlayerConfig implements PlayerConfig {
    private final String objectiveID;
    private final String tabID;
    private final boolean toggle;
    private final UUID uuid;

    public ImmutablePlayerConfig(PlayerConfig pc) {
        this(pc.getDisplayObjectiveID(), pc.getDisplayTabID(), pc.isToggle(), pc.getUUID());
    }

    public ImmutablePlayerConfig(String objectiveID, String tabID, boolean toggle, UUID uuid) {
        this.objectiveID = objectiveID;
        this.tabID = tabID;
        this.toggle = toggle;
        this.uuid = uuid;
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isToggle() {
        return this.toggle;
    }

    @Override
    public void setToggle(boolean toggle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayObjectiveID() {
        return this.objectiveID;
    }

    @Override
    public void setDisplayObjectiveID(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayTabID() {
        return this.tabID;
    }

    @Override
    public void setDisplayTabID(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }
}