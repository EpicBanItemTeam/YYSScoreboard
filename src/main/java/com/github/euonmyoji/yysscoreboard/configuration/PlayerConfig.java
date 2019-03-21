package com.github.euonmyoji.yysscoreboard.configuration;

import java.util.UUID;

/**
 * @author yinyangshi
 */
public interface PlayerConfig {

    String getDisplayObjectiveID();

    void setDisplayObjectiveID(String id);

    String getDisplayTabID();

    void setDisplayTabID(String id);

    UUID getUUID();
}
