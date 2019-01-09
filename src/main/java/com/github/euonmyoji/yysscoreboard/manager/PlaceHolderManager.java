package com.github.euonmyoji.yysscoreboard.manager;

import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import com.github.euonmyoji.yysscoreboard.util.Util;
import me.rojo8399.placeholderapi.PlaceholderService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

/**
 * @author yinyangshi
 */
public class PlaceHolderManager implements TextManager {
    private static PlaceHolderManager instance;

    public static PlaceHolderManager getInstance() {
        if (instance == null) {
            instance = new PlaceHolderManager();
        }
        return instance;
    }

    private final PlaceholderService service;

    @Override
    public Text toText(String s, @Nullable Player p) {
        if (s == null) {
            return Text.EMPTY;
        }
        double tps = Sponge.getServer().getTicksPerSecond();
        s = s.replace("%server_tps%", tps == 20.0 ? "20.0" : String.format("%.2f", tps));
        return PluginConfig.isStaticMode ? service.replacePlaceholders(Util.toText(s), null, null)
                : service.replacePlaceholders(Util.toText(s), p, p);
    }

    private PlaceHolderManager() {
        service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
    }
}
