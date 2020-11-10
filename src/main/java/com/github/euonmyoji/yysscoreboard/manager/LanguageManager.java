package com.github.euonmyoji.yysscoreboard.manager;

import com.github.euonmyoji.yysscoreboard.YysScoreBoard;
import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import org.spongepowered.api.Sponge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author yinyangshi
 */
public class LanguageManager {
    private static ResourceBundle res;
    private static String lang;
    private static Path langFile;

    public static void init() {
        try {
            Files.createDirectories(PluginConfig.cfgDir.resolve("lang"));
        } catch (IOException e) {
            YysScoreBoard.logger.warn("create lang dir error", e);
        }
        for (String lang : new String[]{"lang/en_US.lang", "lang/zh_CN.lang"}) {
            Sponge.getAssetManager().getAsset(YysScoreBoard.plugin, lang)
                    .ifPresent(asset -> {
                        try {
                            asset.copyToFile(PluginConfig.cfgDir.resolve(lang));
                        } catch (IOException e) {
                            YysScoreBoard.logger.warn("copy language file error", e);
                        }
                    });
        }
    }

    private static void check() {
        try {
            Path langFolder = PluginConfig.cfgDir.resolve("lang");
            if (Files.notExists(langFolder)) {
                Files.createDirectory(langFolder);
            }
            try {
                if (Files.notExists(langFile)) {
                    Sponge.getAssetManager().getAsset(YysScoreBoard.plugin, "lang/" + lang + ".lang")
                            .orElseThrow(() -> new FileNotFoundException("asset didn't found language file!"))
                            .copyToFile(langFile);
                }
            } catch (FileNotFoundException ignore) {
                YysScoreBoard.logger.info("locale language file not found");
                langFile = PluginConfig.cfgDir.resolve("lang/en_US.lang");
                Sponge.getAssetManager().getAsset(YysScoreBoard.plugin, "lang/en_US.lang")
                        .orElseThrow(() -> new IOException("asset didn't found language file!"))
                        .copyToFile(langFile);
            }
        } catch (IOException e) {
            YysScoreBoard.logger.error("IOE", e);
        }
    }

    public static String getString(String key) {
        try {
            return res.getString(key);
        } catch (Exception ignore) {
            return key;
        }
    }

    public static void reload() {
        try {
            lang = PluginConfig.getUsingLang();
            langFile = PluginConfig.cfgDir.resolve("lang/" + lang + ".lang");
            check();
            res = new PropertyResourceBundle(Files.newBufferedReader(langFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            YysScoreBoard.logger.error("reload language file error!", e);
        }
    }
}
