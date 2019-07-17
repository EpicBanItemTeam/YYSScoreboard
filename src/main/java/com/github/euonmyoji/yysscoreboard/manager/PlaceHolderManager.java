package com.github.euonmyoji.yysscoreboard.manager;

import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.util.Util;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.impl.utils.TextUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;

import javax.annotation.Nullable;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (PluginConfig.isStableMode) {
            p = null;
        }
        TextTemplate textTemplate = TextUtils.toTemplate(Util.toText(s), PlaceholderService.DEFAULT_PATTERN);
        Map<String, ?> map = service.fillPlaceholders(textTemplate, p, p).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, o -> {
                    Object v = o.getValue();
                    if (v != null) {
                        if (v instanceof Number) {
                            long i = ((Number) v).longValue();
                            double d = ((Number) v).doubleValue();
                            if ((double) i != d) {
                                v = String.format("%.2f", ((Number) v).doubleValue());
                            }
                        }
                        if (v instanceof TemporalAccessor) {
                            v = PluginConfig.timeFormatter.format(((TemporalAccessor) v));
                        }
                    } else {
                        v = o.getKey();
                    }
                    return v;
                }));
        return textTemplate.apply(map).build();
    }
}
