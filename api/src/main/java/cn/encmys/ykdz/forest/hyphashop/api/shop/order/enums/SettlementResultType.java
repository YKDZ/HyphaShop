package cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums;

import org.jetbrains.annotations.NotNull;

public enum SettlementResultType {
    INVALID_CUSTOMER("failure.customer", false),
    CANCELLED("failure.cancelled", false),
    UNKNOWN("failure.unknown", false),
    EMPTY("failure.empty", false),
    TRANSITION_DISABLED("failure.disabled", false),
    NOT_ENOUGH_MONEY("failure.money", true),
    NOT_ENOUGH_PRODUCT("failure.product", true),
    NOT_ENOUGH_INVENTORY_SPACE("failure.inventory-space", true),
    NOT_ENOUGH_PLAYER_STOCK("failure.player-stock", false),
    NOT_ENOUGH_GLOBAL_STOCK("failure.global-stock", false),
    NOT_ENOUGH_MERCHANT_BALANCE("failure.merchant-balance", false),
    NOT_LISTED("failure.not-listed", false),
    SUCCESS("success", true);

    private final @NotNull String configKey;
    private final boolean canBeHandleByPlayer;

    SettlementResultType(@NotNull String configKey, boolean canBeHandleByPlayer) {
        this.configKey = configKey;
        this.canBeHandleByPlayer = canBeHandleByPlayer;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

    public boolean canBeHandleByPlayer() {
        return canBeHandleByPlayer;
    }
}
