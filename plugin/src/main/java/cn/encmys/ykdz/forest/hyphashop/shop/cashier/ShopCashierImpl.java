package cn.encmys.ykdz.forest.hyphashop.shop.cashier;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.ShopCashier;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.currency.MerchantCurrency;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.record.MerchantRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShopCashierImpl implements ShopCashier {
    private final @NotNull Shop shop;
    private final @NotNull Map<@NotNull String, @NotNull MerchantCurrency> currencies;

    public ShopCashierImpl(@NotNull Shop shop, @NotNull Map<String, MerchantRecord> merchant) {
        this.shop = shop;
        this.currencies = merchant.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new MerchantCurrency(entry.getValue()))
                );
    }

    @Override
    public void setCurrencies(@NotNull Map<String, MerchantCurrency> currencies) {
        this.currencies.clear();
        this.currencies.putAll(currencies);
    }

    @Override
    public boolean isMerchant() {
        return !currencies.isEmpty();
    }

    @Override
    public void restockMerchant() {
        if (isMerchant()) {
            currencies.values().forEach(MerchantCurrency::restock);
        }
    }

    @Override
    public @NotNull Shop getShop() {
        return shop;
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull MerchantCurrency> getCurrencies() {
        return currencies;
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Double> getBalances() {
        return currencies.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getBalance()
                ));
    }

    @Override
    public @NotNull Optional<MerchantCurrency> getCurrency(@NotNull String currency) {
        return Optional.ofNullable(currencies.get(currency));
    }
}
