package com.github.euonmyoji.yysscoreboard.util;

import com.github.euonmyoji.yysscoreboard.configuration.PluginConfig;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author yinyangshi
 */
public class Util {

    public static Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }


    public static <T> Stream<T> getStream(Collection<T> list) {
        if (list.size() < PluginConfig.goalCount) {
            return list.stream();
        } else {
            return list.parallelStream();
        }
    }
}
