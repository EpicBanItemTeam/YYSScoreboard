package com.github.euonmyoji.yysscoreboard.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * @author yinyangshi
 */
public class Util {

    public static Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

    public static String replaceTPS(String s) {
        final String tpsPlaceholder = "%server_tps%";
        if (s.contains(tpsPlaceholder)) {
            double tps = Sponge.getServer().getTicksPerSecond();
            s = s.replace("%server_tps%", tps == 20.0 ? "20.0" : String.format("%.2f", tps));
        }
        return s;
    }
}
