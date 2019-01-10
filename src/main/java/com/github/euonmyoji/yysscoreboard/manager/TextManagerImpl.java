package com.github.euonmyoji.yysscoreboard.manager;

import com.github.euonmyoji.yysscoreboard.util.Util;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

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
        s = Util.replaceTPS(s);
        return Util.toText(s);
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
