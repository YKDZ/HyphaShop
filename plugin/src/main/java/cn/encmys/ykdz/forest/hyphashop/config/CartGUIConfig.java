package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.config.record.gui.CartProductIconRecord;
import cn.encmys.ykdz.forest.hyphashop.utils.ConfigUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class CartGUIConfig {
    private static final String cartGUIPath = HyphaShop.INSTANCE.getDataFolder() + "/gui/internal/cart.yml";
    private static final YamlConfiguration cartGUIConfig = new YamlConfiguration();

    private static CartProductIconRecord cartProductIconRecord;

    public static void load() {
        final File file = new File(cartGUIPath);

        if (!file.exists()) {
            HyphaShop.INSTANCE.saveResource("gui/internal/cart.yml", false);
        }

        try {
            cartGUIConfig.load(file);
        } catch (IOException | InvalidConfigurationException error) {
            HyphaShopImpl.LOGGER.error(error.getMessage());
        }

        setup();
    }

    public static void setup() {
        cartProductIconRecord = new CartProductIconRecord(
                ConfigUtils.parseDecorator(getGUIConfig().getConfig("cart-product-icon.icon").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())))
        );
    }

    public static YamlConfiguration getConfig() {
        return cartGUIConfig;
    }

    public static @NotNull ConfigAccessor getGUIConfig() {
        return new ConfigurationSectionAccessor(cartGUIConfig.getConfigurationSection("cart"));
    }

    public static @NotNull CartProductIconRecord getCartProductIconRecord() {
        return cartProductIconRecord;
    }
}
