package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.EnumUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
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
import java.util.Map;

public class MessageConfig {
    //
    private static final @NotNull Map<String, Script> messages_settleResult = new HashMap<>();
    private static final @NotNull Map<String, Script> messages_action = new HashMap<>();
    public static Context scriptContext;
    public static DecimalFormat format_decimal;
    public static String format_timer;
    public static SimpleDateFormat format_date;
    //
    public static String placeholderAPI_cartTotalPrice_notSellToMode;
    //
    public static Script messages_prefix;
    public static Script messages_noPermission;
    public static Script messages_command_reload_success;
    public static Script messages_command_save_success;
    public static Script messages_command_shop_open_success;
    public static Script messages_command_shop_open_failure_invalidShop;
    public static Script messages_command_shop_open_failure_invalidPlayer;
    public static Script messages_command_shop_restock_success;
    public static Script messages_command_shop_restock_failure_invalidShop;
    public static Script messages_command_cart_open_success;
    public static Script messages_command_cart_open_failure_invalidOwnerName;
    public static Script messages_command_history_open_success;
    public static Script messages_command_history_open_failure_invalidOwnerName;
    public static int version;
    private static YamlConfiguration config = new YamlConfiguration();

    public static void load() {
        final String resourcePath = "lang/" + Config.language_message + ".yml";
        final String path = HyphaShop.INSTANCE.getDataFolder() + "/" + resourcePath;

        final File file = new File(path);

        if (!file.exists()) {
            HyphaShop.INSTANCE.saveResource(resourcePath, false);
        }

        try {
            config.load(file);
            InputStream newConfigStream = HyphaShop.INSTANCE.getResource(resourcePath);
            if (newConfigStream == null) {
                LogUtils.error("Resource " + resourcePath + " not found");
                return;
            }
            config = HyphaConfigUtils.merge(config, HyphaConfigUtils.loadYamlFromResource(newConfigStream), path);
            setUp();
        } catch (IOException | InvalidConfigurationException error) {
            LogUtils.error(error.getMessage());
        }
    }

    private static void setUp() {
        scriptContext = ScriptUtils.extractContext(config.getString("context", ""));

        format_decimal = new DecimalFormat(config.getString("format.decimal", "###,###.##"));
        format_timer = config.getString("format.timer", "%02dh:%02dm:%02ds");
        format_date = new SimpleDateFormat(config.getString("format.date.pattern", "MMMM dd, yyyy HH:mm:ss"), HyphaConfigUtils.getLocale(config.getString("format.date.locale", "en_US")));

        placeholderAPI_cartTotalPrice_notSellToMode = config.getString("placeholder-api.cart-total-price.not-sell-to-mode", "Not sell-to mode");

        messages_prefix = StringUtils.wrapToScriptWithOmit(config.getString("messages.prefix", "<gold>HyphaShop <gray>-"));
        messages_noPermission = StringUtils.wrapToScriptWithOmit(getMessage("messages.no-permission"));
        messages_command_reload_success = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.reload.success"));
        messages_command_save_success = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.save.success"));
        messages_command_shop_open_success = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.shop.open.success"));
        messages_command_shop_open_failure_invalidShop = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.shop.open.failure.invalid-shop"));
        messages_command_shop_open_failure_invalidPlayer = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.shop.open.failure.invalid-player"));
        messages_command_history_open_success = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.history.open.success"));
        messages_command_history_open_failure_invalidOwnerName = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.history.open.failure.invalid-owner-name"));
        messages_command_cart_open_success = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.cart.open.success"));
        messages_command_cart_open_failure_invalidOwnerName = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.cart.open.failure.invalid-owner-name"));
        messages_command_shop_restock_success = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.shop.restock.success"));
        messages_command_shop_restock_failure_invalidShop = StringUtils.wrapToScriptWithOmit(getMessage("messages.command.shop.restock.failure.invalid-shop"));
        //
        putSettleResultMessage("direct.sell-to.success");
        putSettleResultMessage("direct.sell-to.failure.money");
        putSettleResultMessage("direct.sell-to.failure.disabled");
        putSettleResultMessage("direct.sell-to.failure.global-stock");
        putSettleResultMessage("direct.sell-to.failure.player-stock");
        putSettleResultMessage("direct.sell-to.failure.inventory-space");
        putSettleResultMessage("direct.buy-from.success");
        putSettleResultMessage("direct.buy-from.failure.disabled");
        putSettleResultMessage("direct.buy-from.failure.product");
        putSettleResultMessage("direct.buy-from.failure.merchant-balance");
        putSettleResultMessage("direct.buy-all-from.success");
        putSettleResultMessage("direct.buy-all-from.failure.disabled");
        putSettleResultMessage("direct.buy-all-from.failure.product");
        putSettleResultMessage("direct.buy-all-from.failure.merchant-balance");
        putSettleResultMessage("cart.sell-to.success");
        putSettleResultMessage("cart.sell-to.partial-success");
        putSettleResultMessage("cart.sell-to.failure.empty");
        putSettleResultMessage("cart.sell-to.failure.not-listed");
        putSettleResultMessage("cart.sell-to.failure.money");
        putSettleResultMessage("cart.sell-to.failure.inventory-space");
        putSettleResultMessage("cart.sell-to.failure.player-stock");
        putSettleResultMessage("cart.sell-to.failure.global-stock");
        putSettleResultMessage("cart.buy-from.success");
        putSettleResultMessage("cart.buy-from.partial-success");
        putSettleResultMessage("cart.buy-from.failure.empty");
        putSettleResultMessage("cart.buy-from.failure.not-listed");
        putSettleResultMessage("cart.buy-from.failure.product");
        putSettleResultMessage("cart.buy-all-from.success");
        putSettleResultMessage("cart.buy-all-from.partial-success");
        putSettleResultMessage("cart.buy-all-from.failure.empty");
        putSettleResultMessage("cart.buy-all-from.failure.not-listed");
        putSettleResultMessage("cart.buy-all-from.failure.product");
        putActionMessage("add-to-cart.success");
        putActionMessage("add-to-cart.failure.not-listed");

        version = config.getInt("version");
    }

    public static YamlConfiguration getConfig() {
        return config;
    }

    private static String getMessage(String path) {
        return config.getString(path, "<red>There may be an error in your language file. The related key is: " + path);
    }

    public static String getTerm(@NotNull OrderType orderType) {
        return config.getString("terms." + EnumUtils.toConfigName(OrderType.class) + "." + EnumUtils.toConfigName(orderType));
    }

    public static String getTerm(@NotNull ShoppingMode shoppingMode) {
        return config.getString("terms." + EnumUtils.toConfigName(ShoppingMode.class) + "." + EnumUtils.toConfigName(shoppingMode));
    }

    private static void putSettleResultMessage(@NotNull String path) {
        messages_settleResult.put(path, StringUtils.wrapToScriptWithOmit(getMessage("messages.settle-result." + path)));
    }

    private static void putActionMessage(@NotNull String path) {
        messages_action.put(path, StringUtils.wrapToScriptWithOmit(getMessage("messages.action." + path)));
    }

    public static @NotNull Script getSettleResultMessage(@NotNull ShoppingMode shoppingMode, @NotNull OrderType orderType, @NotNull SettlementResultType settlementResultType) {
        final String path = shoppingMode.getConfigKey() + "." + orderType.getConfigKey() + "." + settlementResultType.getConfigKey();
        return messages_settleResult.get(path);
    }

    public static @NotNull Script getActionMessage(@NotNull String path) {
        return messages_action.get(path);
    }
}
