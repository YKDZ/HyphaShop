package cn.encmys.ykdz.forest.hyphashop.logger;

import cn.encmys.ykdz.forest.hyphautils.utils.HyphaLogUtils;
import org.jetbrains.annotations.NotNull;

public class Logger implements cn.encmys.ykdz.forest.hyphascript.logger.Logger {
    public void info(@NotNull String log) {
        HyphaLogUtils.info("[HyphaShop]", log);
    }

    public void warn(@NotNull String log) {
        HyphaLogUtils.warn("[HyphaShop]", log);
    }

    public void error(@NotNull String log) {
        HyphaLogUtils.error("[HyphaShop]", log);
    }
}
