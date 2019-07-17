package com.github.euonmyoji.yysscoreboard.manager;

import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.util.Util;
import me.rojo8399.placeholderapi.PlaceholderService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.util.regex.Matcher;

/**
 * @author yinyangshi
 */
public class PlaceHolderManager implements TextManager {
    private static PlaceHolderManager instance;
    public final PlaceholderService service;

    private PlaceHolderManager() {
        service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
    }

    public static PlaceHolderManager getInstance() {
        if (instance == null) {
            instance = new PlaceHolderManager();
        }
        return instance;
    }

    @Override
    public Text toText(String s, @Nullable Player p) {
        if (s == null) {
            return Text.EMPTY;
        }
        Matcher matcher = PlaceholderService.DEFAULT_PATTERN.matcher(s);
        if (PluginConfig.isStableMode) {
            p = null;
        }
        if (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                String v = matcher.group(i);
                Object o = service.parse(v, p, p);
                if (o != null) {
                    if (o instanceof Number) {
                        s = s.replace(v, String.format("%2f", ((Number) o).doubleValue()));
                    } else if (o instanceof Text) {
                        s = s.replace(v, TextSerializers.FORMATTING_CODE.serialize(((Text) o)));
                    } else {
                        s = s.replace(v, o.toString());
                    }
                }
            }
        }

        return Util.toText(s);
    }
}
