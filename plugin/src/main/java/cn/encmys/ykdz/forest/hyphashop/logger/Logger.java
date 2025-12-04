package cn.encmys.ykdz.forest.hyphashop.logger;

import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaLogUtils;
import org.jetbrains.annotations.NotNull;

public class Logger implements cn.encmys.ykdz.forest.hyphascript.logger.Logger {
    public void info(@NotNull String msg) {
        HyphaLogUtils.info("[HyphaShop]", msg.stripIndent().strip());
    }

    public void warn(@NotNull String msg) {
        HyphaLogUtils.warn("[HyphaShop]", msg.stripIndent().strip());
    }

    public void error(@NotNull String msg) {
        HyphaLogUtils.error("[HyphaShop]", msg.stripIndent().strip());
    }

    @Override
    public void debug(@NotNull String msg) {
        if (Config.debug)
            HyphaLogUtils.info("[HyphaShop]", msg.stripIndent().strip());
    }
}
