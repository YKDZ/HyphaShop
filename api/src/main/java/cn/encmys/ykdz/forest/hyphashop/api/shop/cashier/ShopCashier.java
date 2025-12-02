package cn.encmys.ykdz.forest.hyphashop.api.shop.cashier;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.currency.MerchantCurrency;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Check and Checkout the ShopOrder.
 */
public interface ShopCashier {
    void setCurrencies(@NotNull Map<@NotNull String, @NotNull MerchantCurrency> currencies);

    @NotNull Map<@NotNull String, @NotNull MerchantCurrency> getCurrencies();

    @NotNull Map<@NotNull String, @NotNull Double> getBalances();

    @NotNull Optional<MerchantCurrency> getCurrency(@NotNull String currency);

    boolean isMerchant();

    void restockMerchant();

    @NotNull Shop getShop();
}
