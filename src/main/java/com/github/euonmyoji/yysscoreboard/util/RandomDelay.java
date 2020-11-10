package com.github.euonmyoji.yysscoreboard.util;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机延迟数据
 *
 * @author yinyangshi
 */
public class RandomDelay {
    private static final Random R = new Random();
    private final List<Range> ranges = new ArrayList<>();
    private int delay = -1;

    public RandomDelay(String arg) {
        try {
            String[] ors = arg.split(",");
            for (String value : ors) {
                ranges.add(new Range(value.split("~", 2)));
            }
        } catch (Exception e) {
            YysScoreBoard.logger.warn("There is something wrong with random delay: " + arg, e);
        }
    }

    public int getDelay() {
        return delay > 0 ? delay : ranges.size() > 1 ? ranges.get(R.nextInt(ranges.size())).get() : ranges.get(0).get();
    }

    private class Range {
        private final int min;
        private final int max;

        private Range(String[] arg) throws NumberFormatException {
            if (delay != -1) {
                delay = -2;
            }
            if (arg.length == 1) {
                min = Integer.parseInt(arg[0]);
                max = min;
                if (delay == -1) {
                    delay = min;
                }
            } else if (arg.length == 1 + 1) {
                int a = Integer.parseInt(arg[0]);
                int b = Integer.parseInt(arg[1]);
                min = Math.min(a, b);
                max = Math.max(a, b);
            } else {
                throw new IllegalArgumentException("Not a correct delay expression");
            }
        }

        private int get() {
            return min == max ? min : R.nextInt(1 + max - min) + min;
        }
    }
}
