package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProductConfig {
    private static final String path = HyphaShop.INSTANCE.getDataFolder() + "/product";
    private static final HashMap<String, YamlConfiguration> packs = new HashMap<>();

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
                        packs.put(file.getName().replace(".yml", ""), config);
                    } catch (IOException | InvalidConfigurationException error) {
                        HyphaShopImpl.LOGGER.error(error.getMessage());
                    }
                }
            }
        }
    }

    @Nullable
    public static YamlConfiguration getConfig(String packId) {
        return packs.get(packId);
    }

    public static List<String> getAllPacksId() {
        return new ArrayList<>(packs.keySet());
    }

    @Nullable
    public static List<String> getAllProductId(String packId) {
        final YamlConfiguration config = getConfig(packId);
        if (config == null) {
            return null;
        }
        final ConfigurationSection section = config.getConfigurationSection("products");
        if (section == null) {
            return null;
        }
        return Arrays.asList(section.getKeys(false).toArray(new String[0]));
    }
}