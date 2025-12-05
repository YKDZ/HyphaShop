package cn.encmys.ykdz.forest.hyphashop.shop.cashier;

import cn.encmys.ykdz.forest.hyphashop.api.price.PriceInstance;
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

    /**
     * 商人模式下余额是否足以处理此价格<br/>
     * 若不为商人模式则恒为 true
     *
     */
    @Override
    public boolean canBeWithdrew(@NotNull PriceInstance price) {
        if (!isMerchant()) return true;

        for (Map.Entry<@NotNull String, @NotNull Double> entry : price.getPrices().entrySet()) {
            // 不包含即为此种货币不做限制
            if (!currencies.containsKey(entry.getKey())) continue;

            // 余额充足则继续检查下一种货币
            double balance = currencies.get(entry.getKey()).getBalance();
            if (balance >= entry.getValue()) continue;

            return false;
        }

        return true;
    }

    /**
     * 在商店余额扣除指定金额
     *
     */
    @Override
    public void handleWithdraw(@NotNull PriceInstance price) {
        if (!isMerchant()) return;

        for (Map.Entry<@NotNull String, @NotNull Double> entry : price.getPrices().entrySet()) {
            // 不包含即为此种货币不做限制
            if (!currencies.containsKey(entry.getKey())) continue;

            currencies.get(entry.getKey()).modifyBalance(-entry.getValue());
        }
    }

    /**
     * 向商店余额扣除补充指定金额
     *
     */
    @Override
    public void handleDeposit(@NotNull PriceInstance price) {
        if (!isMerchant()) return;

        for (Map.Entry<@NotNull String, @NotNull Double> entry : price.getPrices().entrySet()) {
            if (!currencies.containsKey(entry.getKey())) continue;

            currencies.get(entry.getKey()).modifyBalance(entry.getValue());
        }
    }
}
