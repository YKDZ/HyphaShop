package cn.encmys.ykdz.forest.hyphashop.api.config.action.enums;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

public enum ActionClickType implements ActionableKey {
    LEFT("left"),
    SHIFT_LEFT("shift-left"),
    RIGHT("right"),
    SHIFT_RIGHT("shift-right"),
    WINDOW_BORDER_LEFT("window-border-left"),
    WINDOW_BORDER_RIGHT("window-border-right"),
    MIDDLE("middle"),
    NUMBER_KEY("number-key"),
    DOUBLE_CLICK("double-click"),
    DROP("drop"),
    CONTROL_DROP("control-drop"),
    CREATIVE("creative"),
    SWAP_OFFHAND("swap-offhand"),
    UNKNOWN("unknown");

    private final @NotNull String configKey;

    ActionClickType(@NotNull String configKey) {
        this.configKey = configKey;
    }

    public static @NotNull ActionClickType fromClickType(@NotNull ClickType clickType) {
        return fromConfigKey(clickType.toString().toLowerCase().replaceAll("_", "-"));
    }

    public static @NotNull ActionClickType fromConfigKey(@NotNull String configKey) {
        for (ActionClickType event : ActionClickType.values()) {
            if (event.getConfigKey().equals(configKey)) {
                return event;
            }
        }
        throw new IllegalArgumentException(configKey + " is not a valid config key");
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }
}
