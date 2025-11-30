package cn.encmys.ykdz.forest.hyphashop.hook;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.SettlementLogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Arrays;

public class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
    private static @NotNull String restockTime(@Nullable OfflinePlayer player, @NotNull String params) {
        final Player target = player == null ? null : player.getPlayer();
        if (target == null)
            return "Need a player to work.";

        final String remains = params.replace("restock_time_", "");
        boolean formatted = remains.contains("formatted_");

        final String shopId = remains.replace("formatted_", "");
        final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
        if (shop == null)
            return "Shop " + shopId + " do not exist.";

        if (!formatted) return String.valueOf(shop.getMillisUntilRestock(System.currentTimeMillis()));
        else
            return TextUtils.formatDuration(Duration.ofMillis(shop.getMillisUntilRestock(System.currentTimeMillis())), target);
    }

    private static @NotNull String merchantBalance(@Nullable OfflinePlayer player, @NotNull String params) {
        final Player target = player == null ? null : player.getPlayer();
        if (target == null)
            return "Need a player to work.";

        final String shopId = params.replace("merchant_balance_", "");
        final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
        if (shop == null)
            return "Shop " + shopId + " do not exist.";

        return MessageConfig.getDecimalFormat(player.getPlayer().locale()).format(shop.getShopCashier().getBalance());
    }

    private static @NotNull String shoppingMode(@Nullable OfflinePlayer player, @NotNull String params) {
        final Player target = player == null ? null : player.getPlayer();
        if (target == null)
            return "Need a player to work.";

        final String shopId = params.replace("shopping_mode_", "");
        final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
        if (shop == null)
            return "Shop " + shopId + " do not exist.";

        final Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(target);
        return MessageConfig.getMessageString(MessageConfig.getTermPath(profile.getShoppingMode(shopId)), target.locale().toLanguageTag(), "Term does not exists. Check console for more details");
    }

    private static @NotNull String cartMode(@Nullable OfflinePlayer player) {
        final Player target = player == null ? null : player.getPlayer();
        if (target == null)
            return "Need a player to work.";

        final Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(target);
        return MessageConfig.getMessageString(MessageConfig.getTermPath(profile.getCart().getOrder().getType()), target.locale().toLanguageTag(), "Term does not exists. Check console for more details");
    }

    private static @NotNull String shopHistoryBoughtAmount(@NotNull String params) {
        // %hyphashop_shop_black_market_history_bought_amount_COAL_ORE%
        final String[] data = Arrays.stream(params.split("shop_|_history_bought_amount_")).filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (data.length != 2)
            return "Invalid params.";

        final String shopId = data[0];
        final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
        if (shop == null)
            return "Shop " + shopId + " do not exist.";

        final String productId = data[1];
        final Product product = HyphaShop.PRODUCT_FACTORY.getProduct(productId);
        if (product == null)
            return "Product " + productId + " do not exist.";

        final long historyBuy = SettlementLogUtils.getHistoryAmountFromLogs(productId, OrderType.SELL_TO);
        return String.valueOf(historyBuy);
    }

    private static @NotNull String shopHistoryBoughtStack(@NotNull String params) {
        // %hyphashop_shop_black_market_history_bought_stack_COAL_ORE%
        final String[] data = Arrays.stream(params.split("shop_|_history_bought_stack_")).filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (data.length != 2)
            return "Invalid params.";

        final String shopId = data[0];
        final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
        if (shop == null)
            return "Shop " + shopId + " do not exist.";

        final String productId = data[1];
        final Product product = HyphaShop.PRODUCT_FACTORY.getProduct(productId);
        if (product == null)
            return "Product " + productId + " do not exist.";

        final long historyBuy = SettlementLogUtils.getHistoryStackFromLogs(productId, OrderType.SELL_TO);
        return String.valueOf(historyBuy);
    }

    private static @NotNull String shopHistorySoldAmount(@NotNull String params) {
        // %hyphashop_shop_black_market_history_sold_amount_COAL_ORE%
        final String[] data = Arrays.stream(params.split("shop_|_history_sold_amount_")).filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (data.length != 2)
            return "Invalid params.";

        final String shopId = data[0];
        final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
        if (shop == null)
            return "Shop " + shopId + " do not exist.";

        final String productId = data[1];
        final Product product = HyphaShop.PRODUCT_FACTORY.getProduct(productId);
        if (product == null)
            return "Product " + productId + " do not exist.";

        final long historySell = SettlementLogUtils.getHistoryAmountFromLogs(productId, OrderType.BUY_FROM,
                OrderType.BUY_ALL_FROM);
        return String.valueOf(historySell);
    }

    private static @NotNull String shopHistorySoldStack(@NotNull String params) {
        // %hyphashop_shop_black_market_history_sold_stack_COAL_ORE%
        final String[] data = Arrays.stream(params.split("shop_|_history_sold_stack_")).filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (data.length != 2)
            return "Invalid params.";

        final String shopId = data[0];
        final Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);
        if (shop == null)
            return "Shop " + shopId + " do not exist.";

        final String productId = data[1];
        final Product product = HyphaShop.PRODUCT_FACTORY.getProduct(productId);
        if (product == null)
            return "Product " + productId + " do not exist.";

        final long historySell = SettlementLogUtils.getHistoryAmountFromLogs(productId, OrderType.BUY_FROM,
                OrderType.BUY_ALL_FROM);
        return String.valueOf(historySell);
    }

    @Override
    public @NotNull String getAuthor() {
        return "YK_DZ";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hyphashop";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        if (params.contains("restock_time_")) {
            return restockTime(player, params);
        } else if (params.contains("merchant_balance_")) {
            return merchantBalance(player, params);
        } else if (params.contains("shopping_mode_")) {
            return shoppingMode(player, params);
        } else if (params.contains("cart_mode")) {
            return cartMode(player);
        } else if (params.contains("shop_") && params.contains("_history_bought_amount_")) {
            return shopHistoryBoughtAmount(params);
        } else if (params.contains("shop_") && params.contains("_history_sold_amount_")) {
            return shopHistorySoldAmount(params);
        } else if (params.contains("shop_") && params.contains("_history_bought_stack_")) {
            return shopHistoryBoughtStack(params);
        } else if (params.contains("shop_") && params.contains("_history_sold_stack_")) {
            return shopHistorySoldStack(params);
        }
        return null;
    }
}
