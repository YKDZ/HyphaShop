package cn.encmys.ykdz.forest.hyphashop.api.profile.enums;

import org.jetbrains.annotations.NotNull;

public enum ShoppingMode {
    CART("cart"),
    DIRECT("direct");

    private final @NotNull String configKey;

    ShoppingMode(@NotNull String configKey) {
        this.configKey = configKey;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }
}
