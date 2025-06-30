package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.utils.FileUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NormalGUIConfig {
    private static final @NotNull String path = HyphaShop.INSTANCE.getDataFolder() + "/gui";

    private static final @NotNull List<ConfigAccessor> configs = new ArrayList<>();

    public static void load() {
        try {
            List<File> ymlConfigs = FileUtils.loadYmlFiles(path);
            ymlConfigs.forEach(file -> configs.add(new ConfigurationSectionAccessor(YamlConfiguration.loadConfiguration(file))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<ConfigAccessor> getConfigs() {
        return configs;
    }
}
