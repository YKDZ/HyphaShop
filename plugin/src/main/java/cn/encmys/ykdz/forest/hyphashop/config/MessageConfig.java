package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.EnumUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaConfigUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessageConfig {
    private static final @NotNull Map<@NotNull String, @NotNull YamlConfiguration> configs = new HashMap<>();

    public static void load() {
        final File langDir = new File(HyphaShop.INSTANCE.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        final File[] langFiles = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles == null) {
            LogUtils.error("No language files found in lang/ directory");
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
                LogUtils.error("Failed to load language file " + file.getName() + ": " + error.getMessage());
            }
        }
    }

    public static @NotNull DecimalFormat getDecimalFormat(@NotNull Locale locale) {
        return getDecimalFormat(locale.toLanguageTag().replace("-", "_"));
    }

    public static @NotNull DecimalFormat getDecimalFormat(@NotNull String locale) {
        return new DecimalFormat(getMessage("format.decimal", locale));
    }

    public static @NotNull SimpleDateFormat getDateFormat(@NotNull Locale locale) {
        return getDateFormat(locale.toLanguageTag().replace("-", "_"));
    }

    public static @NotNull SimpleDateFormat getDateFormat(@NotNull String locale) {
        return new SimpleDateFormat(getMessage("format.date.pattern", locale), HyphaConfigUtils.getLocale(getMessage("format.date.locale", locale)));
    }

    public static @NotNull String getMessage(@NotNull String path, @NotNull Locale locale) {
        return getMessage(path, locale.toLanguageTag().replace("-", "_"));
    }

    public static @NotNull String getMessage(@NotNull String path, @NotNull String tag) {
        final YamlConfiguration config = configs.get(tag);
        if (config == null) {
            return "<red>There may be an error in " + tag + ".yml (" + path + ")";
        }
        return config.getString(path, "<red>There may be an error in " + tag + ".yml (" + path + ")");
    }

    public static @NotNull String getTerm(@NotNull ShoppingMode shoppingMode, @NotNull Locale locale) {
        return getTerm(shoppingMode, locale.toLanguageTag().replace("-", "_"));
    }

    public static @NotNull String getTerm(@NotNull OrderType orderType, @NotNull Locale locale) {
        return getTerm(orderType, locale.toLanguageTag().replace("-", "_"));
    }

    public static @NotNull String getTerm(@NotNull OrderType orderType, @NotNull String tag) {
        final String path = "terms." + EnumUtils.toConfigName(OrderType.class) + "."
                + EnumUtils.toConfigName(orderType);
        final YamlConfiguration config = configs.get(tag);
        if (config == null || !config.contains(path)) {
            LogUtils.warn("Message " + path + " not found for tag " + tag + ". Use error message as fallback.");
            return "<red>There may be an error in your language file. The related key is: " + path;
        }
        final String term = config.getString(path);
        assert term != null;
        return term;
    }

    public static @NotNull String getTerm(@NotNull ShoppingMode shoppingMode, @NotNull String tag) {
        final String path = "terms." + EnumUtils.toConfigName(ShoppingMode.class) + "."
                + EnumUtils.toConfigName(shoppingMode);
        final YamlConfiguration config = configs.get(tag);
        if (config == null || !config.contains(path)) {
            LogUtils.warn("Message " + path + " not found for tag " + tag + ". Use error message as fallback.");
            return "<red>There may be an error in your language file. The related key is: " + path;
        }
        final String term = config.getString(path);
        assert term != null;
        return term;
    }

    public static @NotNull Script getSettleResultMessage(@NotNull ShoppingMode shoppingMode,
                                                         @NotNull OrderType orderType, @NotNull SettlementResultType settlementResultType, @NotNull Locale tag) {
        return getSettleResultMessage(shoppingMode, orderType, settlementResultType, tag.toLanguageTag().replace("-", "_"));
    }

    public static @NotNull Script getSettleResultMessage(@NotNull ShoppingMode shoppingMode,
                                                         @NotNull OrderType orderType, @NotNull SettlementResultType settlementResultType, @NotNull String tag) {
        final String path = "messages.settle-result." + shoppingMode.getConfigKey() + "." + orderType.getConfigKey()
                + "." + settlementResultType.getConfigKey();
        final String message = getMessage(path, tag);
        return StringUtils.wrapToScriptWithOmit(message).orElse(new Script("`No message found in " + tag + " (" + path + ")`"));
    }

    public static @NotNull Script getActionMessage(@NotNull String path, @NotNull Locale tag) {
        return getActionMessage(path, tag.toLanguageTag().replace("-", "_"));
    }

    public static @NotNull Script getActionMessage(@NotNull String path, @NotNull String tag) {
        final String fullPath = "messages.action." + path;
        final String message = getMessage(fullPath, tag);
        return StringUtils.wrapToScriptWithOmit(message).orElse(new Script("`No message found in " + tag + " (" + path + ")`"));
    }
}
