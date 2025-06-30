package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaConfigUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Config {
    private static final String resourcePath = "config.yml";
    private static final String path = HyphaShop.INSTANCE.getDataFolder() + "/" + resourcePath;
    public static String language_message;
    public static String language_minecraftLang;
    public static boolean database_sqlite_enabled;
    public static boolean database_mysql_enabled;
    public static boolean database_mysql_url;
    public static boolean priceCorrectByDisableSellOrBuy;
    public static long period_saveData;
    public static long period_checkRestocking;
    public static boolean debug;
    public static int version;
    private static YamlConfiguration config = new YamlConfiguration();

    public static void load() {
        File file = new File(path);

        // 当 config.yml 不存在时尝试初始化所有配置文件
        if (!file.exists()) {
            HyphaShop.INSTANCE.saveResource(resourcePath, false);
            HyphaShop.INSTANCE.saveResource("product/ores.yml", false);
            HyphaShop.INSTANCE.saveResource("product/wools.yml", false);
            HyphaShop.INSTANCE.saveResource("product/sculk.yml", false);
            HyphaShop.INSTANCE.saveResource("product/misc.yml", false);
            HyphaShop.INSTANCE.saveResource("shop/black_market.yml", false);
            HyphaShop.INSTANCE.saveResource("shop/blocks.yml", false);
            HyphaShop.INSTANCE.saveResource("gui/main.yml", false);
            HyphaShop.INSTANCE.saveResource("gui/internal/cart.yml", false);
            HyphaShop.INSTANCE.saveResource("gui/internal/stack-picker.yml", false);
            HyphaShop.INSTANCE.saveResource("gui/internal/order-history.yml", false);
        }

        try {
            config.load(file);
            InputStream newConfigStream = HyphaShop.INSTANCE.getResource(resourcePath);
            if (newConfigStream == null) {
                LogUtils.error("Resource " + resourcePath + " not found");
                return;
            }
            config = HyphaConfigUtils.merge(config, HyphaConfigUtils.loadYamlFromResource(newConfigStream), path);
            setup();
        } catch (IOException | InvalidConfigurationException error) {
            LogUtils.error(error.getMessage());
        }
    }

    private static void setup() {
        language_message = config.getString("language.message", "en_US");
        language_minecraftLang = config.getString("language.minecraft-lang", "en_us").toLowerCase();
        period_saveData = TextUtils.parseTimeStringToTicks(config.getString("period.save-data", "5m"));
        period_checkRestocking = TextUtils.parseTimeStringToTicks(config.getString("period.check-restocking", "3s"));
        priceCorrectByDisableSellOrBuy = config.getBoolean("price-correct-by-disable-sell-or-buy", true);
        database_sqlite_enabled = config.getBoolean("database.sqlite.enabled", true);
        database_mysql_enabled = config.getBoolean("database.mysql.enabled", true);
        database_mysql_url = config.getBoolean("database.mysql.url", true);
        debug = config.getBoolean("debug", false);
        version = config.getInt("version");
    }

    public static YamlConfiguration getConfig() {
        return config;
    }
}
