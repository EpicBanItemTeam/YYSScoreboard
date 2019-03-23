package com.github.euonmyoji.yysscoreboard.util;

//为什么不用setter来检查确保维护性？  因为我也不知道 —— yyssb (点题)

/**
 * 更改值时记得检查immutable
 *
 * @author yinyangshi
 */
public class Pair<T, U> {
    public boolean immutable = false;
    public volatile T first;
    public volatile U second;

    public Pair() {
    }

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
}
