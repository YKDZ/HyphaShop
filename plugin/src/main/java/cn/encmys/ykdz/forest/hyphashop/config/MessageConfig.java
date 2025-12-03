package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.EnumUtils;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaConfigUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageConfig {
    private static final @NotNull Map<@NotNull String, @NotNull YamlConfiguration> configs = new HashMap<>();

    public static void load() {
        final File langDir = new File(HyphaShop.INSTANCE.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        final File[] langFiles = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles == null) {
            HyphaShopImpl.LOGGER.error("No language files found in lang/ directory");
            return;
        }

        for (File file : langFiles) {
            final String tag = file.getName().replace(".yml", "");
            YamlConfiguration config = new YamlConfiguration();

            try {
                config.load(file);
                InputStream newConfigStream = HyphaShop.INSTANCE.getResource("lang/" + file.getName());
                if (newConfigStream != null) {
                    config = HyphaConfigUtils.merge(config, HyphaConfigUtils.loadYamlFromResource(newConfigStream),
                            file.getPath());
                }
                configs.put(tag, config);
            } catch (IOException | InvalidConfigurationException error) {
                HyphaShopImpl.LOGGER.error("Failed to load language file " + file.getName() + ": " + error.getMessage());
            }
        }
    }

    public static @NotNull DecimalFormat getDecimalFormat(@NotNull Locale locale) {
        return getDecimalFormat(locale.toLanguageTag());
    }

    public static @NotNull DecimalFormat getDecimalFormat(@NotNull String locale) {
        return new DecimalFormat(getMessageString("format.decimal", locale, "#######.##"));
    }

    public static @NotNull SimpleDateFormat getDateFormat(@NotNull String locale) {
        return new SimpleDateFormat(getMessageString("format.date.pattern", locale, "MM/dd/yyyy HH:mm:ss"), HyphaConfigUtils.getLocale(getMessageString("format.date.locale", locale, "en_US")));
    }

    @Contract("_, _, !null -> !null")
    public static @Nullable String getMessageString(@NotNull String path, @NotNull String locale, @Nullable String fallback) {
        final YamlConfiguration config = configs.get(locale);

        // 检查指定的配置文件是否存在，并且是否包含路径对应的字符串
        if (config != null && config.isString(path)) {
            return config.getString(path, fallback);
        }

        final YamlConfiguration defaultConfig = configs.get(Config.language_defaultMessage);
        final String defaultLangTag = Config.language_defaultMessage;

        if (defaultConfig != null && defaultConfig.isString(path)) {
            HyphaShopImpl.LOGGER.warn(
                    "Message path '%s' not found in language file '%s.yml'. Falling back to value from default message file ('%s.yml')."
                            .formatted(path, locale, defaultLangTag)
            );
            return defaultConfig.getString(path, fallback);
        }

        String warningMessage;

        if (defaultConfig == null) {
            warningMessage =
                    "Message path '%s' not found in language file '%s.yml'. Default message file ('%s.yml') is also missing. Using provided fallback message: '%s'."
                            .formatted(path, locale, defaultLangTag, fallback);
        } else {
            warningMessage =
                    "Message path '%s' not found in language file '%s.yml'. Although default message file ('%s.yml') exists, it does not define path '%s'. Using provided fallback message: '%s'."
                            .formatted(path, locale, defaultLangTag, path, fallback);
        }

        HyphaShopImpl.LOGGER.warn(warningMessage + " Please ensure the configuration files are correctly structured.");
        return fallback;
    }

    public static @NotNull Optional<Script> getMessageScript(@NotNull String path, @NotNull String locale) {
        return StringUtils.wrapToScriptWithOmit(getMessageString(path, locale, null));
    }

    public static @NotNull @Unmodifiable List<String> getMessageList(@NotNull String path, @NotNull String tag) {
        final YamlConfiguration config = configs.get(tag);
        if (config == null) {
            return List.of();
        }
        return config.getStringList(path);
    }

    public static @NotNull String getActionMessagePath(@NotNull String path) {
        return "messages.action." + path;
    }

    public static @NotNull String getSettleResultMessagePath(@NotNull ShoppingMode shoppingMode, @NotNull OrderType orderType, @NotNull SettlementResultType settlementResultType) {
        return "messages.settle-result." + shoppingMode.getConfigKey() + "." + orderType.getConfigKey()
                + "." + settlementResultType.getConfigKey();
    }

    public static @NotNull String getTermPath(@NotNull ShoppingMode shoppingMode) {
        return "terms." + EnumUtils.toConfigName(ShoppingMode.class) + "."
                + EnumUtils.toConfigName(shoppingMode);
    }

    public static @NotNull String getTermPath(@NotNull OrderType orderType) {
        return "terms." + EnumUtils.toConfigName(OrderType.class) + "."
                + EnumUtils.toConfigName(orderType);
    }
}
