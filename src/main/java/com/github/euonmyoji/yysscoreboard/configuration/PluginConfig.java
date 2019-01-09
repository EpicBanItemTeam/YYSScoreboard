package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.util.Locale;

/**
 * @author yinyangshi
 */

public final class PluginConfig {
    private static final String LANGUAGE = "lang";
    private static final String STABLE_MODE = "stable";
    private static final String STATIC_MODE = "static";
    private static final String SAME_SCORE_MODE = "same-score";
    private static final String UPDATE_TICK = "update-interval-tick";

    public static boolean isStableMode = true;
    public static boolean isStaticMode = false;
    static boolean hasSameScore = false;
    private static int updateTick = 1;
    private static CommentedConfigurationNode cfg;
    private static CommentedConfigurationNode generalNode;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private PluginConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(YysScoreBoard.plugin.cfgDir.resolve("config.conf")).build();
        reload();
        setComment();
        save();
    }

    @SuppressWarnings("unused")
    public static String getUsingLang() {
        return generalNode.getNode(LANGUAGE).getString(Locale.getDefault().toString());
    }

    public static int getUpdateTick() {
        return updateTick;
    }

    public static void reload() {
        loadNode();
        generalNode.getNode(LANGUAGE).getString(Locale.getDefault().toString());
        CommentedConfigurationNode modes = generalNode.getNode("scoreboard-mode");
        isStableMode = modes.getNode(STABLE_MODE).getBoolean(true);
        isStaticMode = modes.getNode(STATIC_MODE).getBoolean(false);
        hasSameScore = modes.getNode(SAME_SCORE_MODE).getBoolean(false);
        updateTick = generalNode.getNode(UPDATE_TICK).getInt(20);
    }

    private static void save() {
        try {
            loader.save(cfg);
        } catch (IOException e) {
            YysScoreBoard.logger.warn("error when saving plugin config", e);
        }
    }

    private static void loadNode() {
        try {
            cfg = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
        } catch (IOException e) {
            YysScoreBoard.logger.warn("load plugin config failed", e);
        }
        generalNode = cfg.getNode("general");
    }

    private static void setComment() {
        generalNode.getNode(UPDATE_TICK).setComment(generalNode.getNode(UPDATE_TICK).getComment()
                .orElse("记分板刷新时间 默认为20tick" +
                        "\n重启生效"));

        generalNode.getNode("scoreboard-mode").setComment(generalNode.getNode("scoreboard-mode").getComment()
                .orElse("计分板模式选项(更好的配置" +
                        "\n·stable: 兼容模式(兼容newhonor) 可能会遇到神奇的bug(例如玩家变量错乱了)  #具体代码实现为 所有sb为玩家目前身上的sb" +
                        "\n·static: 计分板是否为静态(即全服统一sb 不分玩家) 如果为静态模式 请不要使用有关玩家的变量！！" +
                        "\n·same-score: 是否允许计分板中文本有相同数字(如果不允许 那么闪烁可能轻微一点)"));
    }
}
