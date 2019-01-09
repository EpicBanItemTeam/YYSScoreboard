package com.github.euonmyoji.yysscoreboard.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * @author yinyangshi
 */
public class Util {

    public static Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }
}
