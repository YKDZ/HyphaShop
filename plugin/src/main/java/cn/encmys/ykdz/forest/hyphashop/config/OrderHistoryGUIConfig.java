package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.config.record.gui.HistoryIconRecord;
import cn.encmys.ykdz.forest.hyphashop.utils.ConfigUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class OrderHistoryGUIConfig {
    private static final String orderHistoryGUIPath = HyphaShop.INSTANCE.getDataFolder() + "/gui/internal/order-history.yml";
    private static final YamlConfiguration orderHistoryGUIConfig = new YamlConfiguration();

    private static HistoryIconRecord historyIconRecord;

    public static void load() {
        final File file = new File(orderHistoryGUIPath);

        if (!file.exists()) {
            HyphaShop.INSTANCE.saveResource("gui/internal/order-history.yml", false);
        }

        try {
            orderHistoryGUIConfig.load(file);
        } catch (IOException | InvalidConfigurationException error) {
            LogUtils.error(error.getMessage());
        }

        setup();
    }

    public static void setup() {
        historyIconRecord = new HistoryIconRecord(
                ConfigUtils.parseDecorator(getGUIConfig().getConfig("history-icon.icon").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()))),
                StringUtils.wrapToScriptWithOmit(getGUIConfig().getString("history-icon.format.order-content-line").orElse("<red>Order content line not found!")).orElse(new Script("`<red>Order content line not found!`")),
                ConfigUtils.parseDecorator(getGUIConfig().getConfig("history-placeholder-icon.icon").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())))
        );
    }

    public static YamlConfiguration getConfig() {
        return orderHistoryGUIConfig;
    }

    public static @NotNull ConfigAccessor getGUIConfig() {
        return new ConfigurationSectionAccessor(orderHistoryGUIConfig.getConfigurationSection("order-history"));
    }

    public static @NotNull HistoryIconRecord getHistoryIconRecord() {
        return historyIconRecord;
    }
}
