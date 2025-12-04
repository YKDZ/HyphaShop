package cn.encmys.ykdz.forest.hyphashop.price;

import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceMode;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceProperty;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.currency.manager.CurrencyManagerImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PriceImpl extends Price {
    public PriceImpl(@NotNull ConfigAccessor defaultConfig, @NotNull ConfigAccessor config) {
        super(HyphaShop.CURRENCY_MANAGER.getCurrency(
                                config.getString("currency")
                                        .orElse(defaultConfig.getString("currency")
                                                .orElse("VAULT")
                                        )
                        )
                        .orElse(CurrencyManagerImpl.VAULT_CURRENCY)
        );

        if (config.contains("fixed")) {
            priceMode = PriceMode.FIXED;
            this.setProperty(PriceProperty.FIXED, config.getDouble("fixed").orElse(Double.NaN));
        } else if (config.contains("mean") || config.contains("dev")) {
            priceMode = PriceMode.GAUSSIAN;
            this.setProperty(PriceProperty.MEAN, config.getDouble("mean").orElse(defaultConfig.getDouble("mean").orElse(Double.NaN)))
                    .setProperty(PriceProperty.DEV, config.getDouble("dev").orElse(defaultConfig.getDouble("dev").orElse(Double.NaN)))
                    .setProperty(PriceProperty.ROUND, config.getBoolean("round").orElse(defaultConfig.getBoolean("round").orElse(false)));
        } else if (config.contains("min") || config.contains("max")) {
            priceMode = PriceMode.MINMAX;
            this.setProperty(PriceProperty.MIN, config.getDouble("min").orElse(defaultConfig.getDouble("min").orElse(Double.NaN)))
                    .setProperty(PriceProperty.MAX, config.getDouble("max").orElse(defaultConfig.getDouble("max").orElse(Double.NaN)))
                    .setProperty(PriceProperty.ROUND, config.getBoolean("round").orElse(defaultConfig.getBoolean("round").orElse(false)));
        } else if (config.contains("formula") || config.contains("context")) {
            priceMode = PriceMode.FORMULA;
            final String formula = config.getString("formula").orElse(defaultConfig.getString("formula").orElse(null));
            final String context = config.getString("context").orElse(defaultConfig.getString("context").orElse(null));

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
            throw new IllegalArgumentException("Invalid price setting: " + config + " and default configs: " + defaultConfig + ". The price will be disabled.");
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
        return switch (priceMode) {
            case GAUSSIAN -> {
                Boolean round = getProperty(PriceProperty.ROUND);
                Double mean = getProperty(PriceProperty.MEAN);
                Double dev = getProperty(PriceProperty.DEV);
                if (round == null || mean == null || dev == null || Double.isNaN(mean) || Double.isNaN(dev)) {
                    HyphaShopImpl.LOGGER.warn("Invalid price property: " + properties + ". Price will be disabled.");
                    this.priceMode = PriceMode.DISABLE;
                    yield Optional.empty();
                }
                double result = mean + dev * random.nextGaussian();
                if (result < 0) {
                    HyphaShopImpl.LOGGER.warn("A negative price was generated: " + properties + ". Price will be disabled for safety.");
                    this.priceMode = PriceMode.DISABLE;
                    yield Optional.empty();
                }
                yield Optional.of(round ? Math.floor(result) : result);
            }
            case FIXED -> {
                Double fixed = getProperty(PriceProperty.FIXED);
                if (fixed == null) {
                    HyphaShopImpl.LOGGER.warn("Invalid price property: " + properties + ".");
                    this.priceMode = PriceMode.DISABLE;
                    yield Optional.empty();
                }
                if (fixed < 0) {
                    HyphaShopImpl.LOGGER.warn("A negative price was generated: " + properties + ". Price will be disabled for safety.");
                    this.priceMode = PriceMode.DISABLE;
                    yield Optional.empty();
                }
                yield Optional.of(fixed);
            }
            case MINMAX -> {
                Double min = getProperty(PriceProperty.MIN);
                Double max = getProperty(PriceProperty.MAX);
                Boolean round = getProperty(PriceProperty.ROUND);
                if (round == null || min == null || max == null || Double.isNaN(min) || Double.isNaN(max)) {
                    HyphaShopImpl.LOGGER.warn("Invalid price property: " + properties + ". Price will be disabled.");
                    this.priceMode = PriceMode.DISABLE;
                    yield Optional.empty();
                }
                double result = min + (max - min) * random.nextDouble();
                if (result < 0) {
                    HyphaShopImpl.LOGGER.warn("A negative price was generated: " + properties + ". Price will be disabled for safety.");
                    this.priceMode = PriceMode.DISABLE;
                    yield Optional.empty();
                }
                yield Optional.of(round ? Math.floor(result) : result);
            }
            default -> Optional.empty();
        };
    }

    @Override
    public @NotNull PriceMode getPriceMode() {
        return priceMode;
    }
}