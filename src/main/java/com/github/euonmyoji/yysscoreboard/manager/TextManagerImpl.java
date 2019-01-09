package com.github.euonmyoji.yysscoreboard.manager;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

/**
 * @author yinyangshi
 */
public class TextManagerImpl implements TextManager {

    @Override
    public Text toText(String s, @Nullable Player p) {
        if (s == null) {
            return Text.EMPTY;
        }
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    private TextManagerImpl() {
    }

    private static TextManagerImpl instance;

    public static TextManagerImpl getInstance() {
        if (instance == null) {
            instance = new TextManagerImpl();
        }
        return instance;
    }
}
