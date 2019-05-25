package com.github.euonmyoji.yysscoreboard.data;

//为什么不用setter来检查确保维护性？  因为我也不知道 —— yyssb (点题)

/**
 * 更改值时记得检查immutable
 *
 * @author yinyangshi
 */
public class DisplayIDData {
    public boolean immutable = false;
    public volatile String objectiveID;
    public volatile String tabID;
    public boolean once = false;

    public DisplayIDData(String first, String tabID) {
        this.objectiveID = first;
        this.tabID = tabID;
    }
}
