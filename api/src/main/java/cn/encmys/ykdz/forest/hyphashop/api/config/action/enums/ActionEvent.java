package cn.encmys.ykdz.forest.hyphashop.api.config.action.enums;

import org.jetbrains.annotations.NotNull;

public enum ActionEvent implements ActionableKey {
    // 商店
    SHOP_ON_RESTOCK("on-restock"),
    // 商品
    PRODUCT_ON_GIVE("on-give"),
    PRODUCT_ON_MATCH("on-match"),
    PRODUCT_ON_TAKE("on-take"),
    PRODUCT_ON_BEFORE_LIST("on-before-list"),
    PRODUCT_ON_AFTER_LIST("on-after-list"),
    // GUI
    GUI_ON_OPEN("on-open"),
    GUI_ON_CLOSE("on-close"),
    GUI_ON_OUTSIDE_CLICK("on-outside-click");

    private final @NotNull String configKey;

    ActionEvent(@NotNull String configKey) {
        this.configKey = configKey;
    }

    public static @NotNull ActionEvent fromConfigKey(@NotNull String configKey) {
        for (ActionEvent event : ActionEvent.values()) {
            if (event.getConfigKey().equals(configKey)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown action event key: " + configKey);
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }
}
