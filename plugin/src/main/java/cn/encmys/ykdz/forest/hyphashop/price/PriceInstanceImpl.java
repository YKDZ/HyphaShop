package cn.encmys.ykdz.forest.hyphashop.price;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.currency.CurrencyProvider;
import cn.encmys.ykdz.forest.hyphashop.api.price.PriceInstance;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PriceInstanceImpl implements PriceInstance {
    /**
     * currencyId:amount
     */
    private final @NotNull Map<@NotNull String, @Positive Double> prices = new HashMap<>();

    public PriceInstanceImpl() {
    }

    public PriceInstanceImpl(@NotNull Map<@NotNull String, @Positive Double> prices) {
        this.prices.putAll(prices);
    }

    @Override
    public @NotNull Map<String, Double> getPrices() {
        return prices;
    }

    @Override
    public @NotNull Optional<Double> getPrice(@NotNull String id) {
        return Optional.ofNullable(prices.get(id));
    }

    @Override
    public boolean isEmpty() {
        return prices.isEmpty();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Double> mul(double stack) {
        return prices.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> e.getValue() * stack));
    }

    /**
     * 将给定价格合并到此价格实例中<br/>
     * 冲突则值相加
     *
     * @return 合并后的原对象
     */
    @Override
    public @NotNull PriceInstance merge(@NotNull Map<String, Double> target) {
        for (Map.Entry<String, Double> entry : target.entrySet()) {
            prices.merge(entry.getKey(), entry.getValue(), Double::sum);
        }
        return this;
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer customer) {
        for (Map.Entry<@NotNull String, @NotNull Double> entry : prices.entrySet()) {
            final String currencyId = entry.getKey();
            final double price = entry.getValue();
            final CurrencyProvider currency = HyphaShop.CURRENCY_MANAGER.getCurrency(currencyId).orElse(null);

            if (currency == null) {
                HyphaShop.INSTANCE.getLogger().severe("""
                        CurrencyProvider %s doesn't exist. This is a bug. Please report it.
                        """.formatted(currencyId));
                return false;
            }

            if (!currency.withdraw(customer, price)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean deposit(@NotNull OfflinePlayer customer) {
        for (Map.Entry<@NotNull String, @NotNull Double> entry : prices.entrySet()) {
            final String currencyId = entry.getKey();
            final double price = entry.getValue();
            final CurrencyProvider currency = HyphaShop.CURRENCY_MANAGER.getCurrency(currencyId).orElse(null);

            if (currency == null) {
                HyphaShop.INSTANCE.getLogger().severe("""
                        CurrencyProvider %s doesn't exist. This is a bug. Please report it.
                        """.formatted(currencyId));
                return false;
            }

            if (!currency.deposit(customer, price)) {
                return false;
            }
        }

        return true;
    }
}
