package cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums;

import org.jetbrains.annotations.NotNull;

public enum OrderType {
    BUY_FROM("buy-from"),
    BUY_ALL_FROM("buy-all-from"),
    SELL_TO("sell-to");

    private final @NotNull String configKey;

    OrderType(@NotNull String configKey) {
        this.configKey = configKey;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }
}
