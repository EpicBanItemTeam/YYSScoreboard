package com.github.euonmyoji.yysscoreboard.util;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;

import java.util.Random;

/**
 * @author yinyangshi
 */
public class RandomID {
    private final Random r = new Random();
    private String[] ids;

    public RandomID(String arg) {
        try {
            ids = arg.split(",");
        } catch (Exception e) {
            YysScoreBoard.logger.warn("There is something wrong with EffectsDelay: " + arg, e);
        }
    }

    public String getID() {
        return ids.length > 1 ? ids[r.nextInt(ids.length)] : ids[0];
    }
}
