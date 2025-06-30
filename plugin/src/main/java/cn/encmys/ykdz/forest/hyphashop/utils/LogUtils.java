package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphautils.utils.HyphaLogUtils;
import org.jetbrains.annotations.NotNull;

public class LogUtils {
    public static void info(@NotNull String log) {
        HyphaLogUtils.info("[HyphaShop]", log);
    }

    public static void warn(@NotNull String log) {
        HyphaLogUtils.warn("[HyphaShop]", log);
    }

    public static void error(@NotNull String log) {
        HyphaLogUtils.error("[HyphaShop]", log);
    }
}
