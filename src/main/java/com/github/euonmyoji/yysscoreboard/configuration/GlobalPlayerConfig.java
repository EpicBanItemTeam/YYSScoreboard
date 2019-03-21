package com.github.euonmyoji.yysscoreboard.configuration;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.util.TypeTokens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author yinyangshi
 */
public class GlobalPlayerConfig {
    public static List<UUID> list = new ArrayList<>();
    private static CommentedConfigurationNode cfg;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;

    private GlobalPlayerConfig() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        loader = HoconConfigurationLoader.builder()
                .setPath(YysScoreBoard.plugin.cfgDir.resolve("playerdata.conf")).build();
        reload();
        save();
    }

    public static void reload() {
        loadNode();
        try {
            list = cfg.getNode("off").getList(TypeTokens.STRING_TOKEN, new ArrayList<>())
                    .stream().map(UUID::fromString).collect(Collectors.toList());
        } catch (ObjectMappingException e) {
            YysScoreBoard.logger.warn("error about playerdata", e);
        }
    }

    public static void saveList() throws ObjectMappingException {
        cfg.getNode("off").setValue(new TypeToken<List<String>>() {
        }, list.stream().map(UUID::toString).collect(Collectors.toList()));
        save();
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
    }
}
