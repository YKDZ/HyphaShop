package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.record.MerchantRecord;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.config.record.shop.ShopSettingsRecord;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShopConfig {
    private static final @NotNull String path = HyphaShop.INSTANCE.getDataFolder() + "/shop";
    private static final @NotNull Map<@NotNull String, @NotNull YamlConfiguration> configs = new HashMap<>();

    public static void load() {
        final File directory = new File(path);

        if (!directory.exists() || !directory.isDirectory()) {
            directory.getParentFile().mkdirs();
        }

        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    final YamlConfiguration config = new YamlConfiguration();
                    try {
                        config.load(file);
                        configs.put(file.getName().replace(".yml", ""), config);
                    } catch (IOException | InvalidConfigurationException error) {
                        HyphaShopImpl.LOGGER.error(error.getMessage());
                    }
                }
            }
        }
    }

    public static @NotNull YamlConfiguration getConfig(@NotNull String shopId) {
        return configs.get(shopId);
    }

    public static @NotNull List<String> getAllId() {
        return new ArrayList<>(configs.keySet());
    }

    @Contract("_ -> new")
    public static @NotNull ShopSettingsRecord getShopSettingsRecord(@NotNull String shopId) {
        final ConfigAccessor config = getShopSettingsConfig(shopId);
        return new ShopSettingsRecord(
                config.getInt("size").orElse(Integer.MIN_VALUE),
                config.getString("name").orElse("<red>Shop name not found!"),
                config.getBoolean("auto-restock.enabled").orElse(false),
                TextUtils.parseTimeStringToTicks(config.getString("auto-restock.period").orElse("0s")),
                getMerchantRecord(shopId),
                config.getString("context").orElse(""),
                ActionsConfig.of(config.getConfig("actions").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())))
        );
    }

    public static @NotNull List<String> getAllProductsId(@NotNull String shopId) {
        return getConfig(shopId).getStringList("products");
    }

    public static @NotNull Map<String, MerchantRecord> getMerchantRecord(@NotNull String shopId) {
        final ConfigAccessor config = getShopSettingsConfig(shopId).getConfig("merchant").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));
        return config.getLocalMembers().map(map -> map.entrySet()
                .stream()
                .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                (entry) -> new MerchantRecord(
                                        entry.getValue().getDouble("balance").orElse(0d),
                                        entry.getValue().getBoolean("replenish").orElse(false),
                                        entry.getValue().getBoolean("overflow").orElse(false),
                                        entry.getValue().getBoolean("inherit").orElse(false)
                                )
                        )
                )).orElse(Map.of());
    }

    public static @NotNull ConfigAccessor getShopSettingsConfig(@NotNull String shopId) {
        return new ConfigurationSectionAccessor(getConfig(shopId).getConfigurationSection("settings"));
    }

    public static @NotNull ConfigAccessor getShopGUIConfig(@NotNull String shopId) {
        return new ConfigurationSectionAccessor(getConfig(shopId).getConfigurationSection("shop-gui"));
    }
}
