package com.github.euonmyoji.yysscoreboard.manager;

import com.github.euonmyoji.yysscoreboard.util.Util;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

/**
 * @author yinyangshi
 */
public class TextManagerImpl implements TextManager {

    private static TextManagerImpl instance;

    private TextManagerImpl() {
    }

    public static TextManagerImpl getInstance() {
        if (instance == null) {
            instance = new TextManagerImpl();
        }
        return instance;
    }

    @Override
    public Text toText(String s, @Nullable Player p) {
        if (s == null) {
            return Text.EMPTY;
        }
        return Util.toText(s);
    }
}
