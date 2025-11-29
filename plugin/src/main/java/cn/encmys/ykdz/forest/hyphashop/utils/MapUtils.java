package cn.encmys.ykdz.forest.hyphashop.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MapUtils {
    public static <K, V> @NotNull @Unmodifiable Map<K, V> buildImmutableMap(@NotNull Consumer<Map<K, V>> mutator) {
        final Map<K, V> tmp = new HashMap<>();
        mutator.accept(tmp);
        return Collections.unmodifiableMap(tmp);
    }
}
