package cn.encmys.ykdz.forest.hyphashop.api.price;

import cn.encmys.ykdz.forest.hyphashop.api.currency.CurrencyProvider;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceMode;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public abstract class Price {
    protected static final @NotNull Random random = new SecureRandom();

    protected final @NotNull Map<@NotNull PriceProperty, @Nullable Object> properties = new HashMap<>();
    protected final @NotNull CurrencyProvider currencyProvider;
    protected @NotNull PriceMode priceMode = PriceMode.FIXED;

    public Price(@NotNull CurrencyProvider currencyProvider) {
        this.currencyProvider = currencyProvider;
    }

    public @NotNull Price setProperty(@NotNull PriceProperty type, @Nullable Object value) {
        properties.put(type, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getProperty(@NotNull PriceProperty type) {
        Object value = properties.get(type);
        if (value == null) return null;
        else if (type.getToken().getRawType().isInstance(value)) {
            return (T) type.getToken().getRawType().cast(value);
        }
        throw new IllegalArgumentException("Invalid type for config key: " + type);
    }

    public @NotNull CurrencyProvider getCurrencyProvider() {
        return currencyProvider;
    }

    public abstract @NotNull Optional<Double> getNewPrice();

    public abstract @NotNull PriceMode getPriceMode();
}
