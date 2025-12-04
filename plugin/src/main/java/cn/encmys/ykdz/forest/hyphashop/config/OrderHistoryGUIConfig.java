package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class OrderHistoryGUIConfig {
    private static final @NotNull String orderHistoryGUIPath = HyphaShop.INSTANCE.getDataFolder() + "/gui/internal/order-history.yml";
    private static final @NotNull YamlConfiguration orderHistoryGUIConfig = new YamlConfiguration();

    public static void load() {
        final File file = new File(orderHistoryGUIPath);

        if (!file.exists()) {
            HyphaShop.INSTANCE.saveResource("gui/internal/order-history.yml", false);
        }

        try {
            orderHistoryGUIConfig.load(file);
        } catch (IOException | InvalidConfigurationException error) {
            HyphaShopImpl.LOGGER.error(error.getMessage());
        }
    }

    public static YamlConfiguration getConfig() {
        return orderHistoryGUIConfig;
    }

    public static @NotNull ConfigAccessor getGUIConfig() {
        return new ConfigurationSectionAccessor(orderHistoryGUIConfig.getConfigurationSection("order-history"));
    }
}
