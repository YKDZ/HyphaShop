package cn.encmys.ykdz.forest.hyphashop.price;

import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceMode;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceProperty;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.config.CurrencyConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.MathUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PriceImpl extends Price {
    public PriceImpl(@NotNull ConfigAccessor config) {
        super(HyphaShop.CURRENCY_MANAGER.getCurrency(config.getString("currency")
                        .orElse(CurrencyConfig.getBaseCurrency()))
                .orElseThrow(() -> new IllegalArgumentException("""
                        Currency %s not exists. This price will be skipped.
                        """.formatted(config.getString("currency")
                        .orElse(CurrencyConfig.getBaseCurrency())))));

        this.setProperty(PriceProperty.ROUND, config.getInt("round").orElse(null));

        if (config.selfContains("fixed")) {
            priceMode = PriceMode.FIXED;
            this.setProperty(PriceProperty.FIXED, config.getDouble("fixed").orElse(Double.NaN));
        } else if (config.selfContains("mean") || config.selfContains("dev")) {
            priceMode = PriceMode.GAUSSIAN;
            this.setProperty(PriceProperty.MEAN, config.getDouble("mean").orElse(Double.NaN))
                    .setProperty(PriceProperty.DEV, config.getDouble("dev").orElse(Double.NaN));
        } else if (config.selfContains("min") || config.selfContains("max")) {
            priceMode = PriceMode.MINMAX;
            this.setProperty(PriceProperty.MIN, config.getDouble("min"))
                    .setProperty(PriceProperty.MAX, config.getDouble("max"));
        } else if (config.selfContains("formula") || config.selfContains("context")) {
            priceMode = PriceMode.FORMULA;
            final String formula = config.getString("formula").orElse(null);
            final String context = config.getString("context").orElse(null);

            if (formula == null) {
                priceMode = PriceMode.DISABLE;
                HyphaShopImpl.LOGGER.warn("Invalid price setting: " + config + ". The price will be disabled.");
                return;
            }

            this.setProperty(PriceProperty.FORMULA, StringUtils.wrapToScript(formula))
                    .setProperty(PriceProperty.CONTEXT, context == null ? InternalObjectManager.GLOBAL_OBJECT : ScriptUtils.extractContext(context));
        } else if (config.getBoolean("disable").orElse(false)) {
            priceMode = PriceMode.DISABLE;
        } else {
            priceMode = PriceMode.DISABLE;
            throw new IllegalArgumentException("Invalid price setting: " + config + ". The price will be disabled.");
        }
    }

    /**
     * 返回从这个 Price 配置产生的一个新的价格数值<br/>
     * 若配置出错或本就没有数值（比如 {@link PriceMode#DISABLE} 时）<br/>
     * 则返回一个 {@link Double#NaN}
     *
     */
    @Override
    public @NotNull Optional<Double> getNewPrice() {
        // 提前获取 round 属性，确保所有分支都能使用
        final @Nullable Integer round = getProperty(PriceProperty.ROUND);

        return switch (priceMode) {
            case GAUSSIAN -> {
                Double mean = getProperty(PriceProperty.MEAN);
                Double dev = getProperty(PriceProperty.DEV);

                if (isInvalidProperty(mean) || isInvalidProperty(dev)) {
                    yield disablePrice("Invalid price property: " + properties + ".");
                }

                double result = mean + dev * random.nextGaussian();

                yield processResult(result, round);
            }
            case FIXED -> {
                Double fixed = getProperty(PriceProperty.FIXED);

                if (isInvalidProperty(fixed)) {
                    yield disablePrice("Invalid price property for FIXED mode: " + properties + ".");
                }

                yield processResult(fixed, round);
            }
            case MINMAX -> {
                Double min = getProperty(PriceProperty.MIN);
                Double max = getProperty(PriceProperty.MAX);

                if (isInvalidProperty(min) || isInvalidProperty(max)) {
                    yield disablePrice("Invalid price property: " + properties + ".");
                }

                double result = min + (max - min) * random.nextDouble();

                yield processResult(result, round);
            }
            case DISABLE -> Optional.empty(); // 显式处理 DISABLE 模式
            default -> Optional.empty();
        };
    }

    private boolean isInvalidProperty(@Nullable Double value) {
        return value == null || value.isNaN() || value.isInfinite();
    }

    private @NotNull Optional<Double> processResult(double result, @Nullable Integer round) {
        if (result < 0) {
            return disablePrice("A negative price (" + result + ") was generated: " + properties + ". Price will be disabled for safety.");
        }

        // 四舍五入
        double finalPrice = round != null
                ? MathUtils.round(result, round).doubleValue()
                : result;

        return Optional.of(finalPrice);
    }

    private @NotNull Optional<Double> disablePrice(String reason) {
        HyphaShopImpl.LOGGER.warn(reason);
        this.priceMode = PriceMode.DISABLE;
        return Optional.empty();
    }

    @Override
    public @NotNull PriceMode getPriceMode() {
        return priceMode;
    }
}