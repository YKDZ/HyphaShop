package cn.encmys.ykdz.forest.hyphashop.price;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceMode;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceProperty;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import org.jetbrains.annotations.NotNull;

public class PriceImpl extends Price {
    public PriceImpl(@NotNull ConfigAccessor defaultConfig, @NotNull ConfigAccessor config) {
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
        } else if (config.getBoolean("bundle-auto-new").orElse(false)) {
            priceMode = PriceMode.BUNDLE_AUTO_NEW;
        } else if (config.getBoolean("bundle-auto-reuse").orElse(false)) {
            priceMode = PriceMode.BUNDLE_AUTO_REUSE;
        } else if (config.contains("formula") || config.contains("context")) {
            priceMode = PriceMode.FORMULA;
            final String formula = config.getString("formula").orElse(defaultConfig.getString("formula").orElse(null));
            final String context = config.getString("context").orElse(defaultConfig.getString("context").orElse(null));

            if (formula == null) {
                priceMode = PriceMode.DISABLE;
                LogUtils.warn("Invalid price setting: " + config + ". The price will be disabled.");
                return;
            }

            this.setProperty(PriceProperty.FORMULA, StringUtils.wrapToScript(formula))
                    .setProperty(PriceProperty.CONTEXT, context == null ? Context.GLOBAL_OBJECT : ScriptUtils.extractContext(context));
        } else if (config.getBoolean("disable").orElse(false)) {
            priceMode = PriceMode.DISABLE;
        } else {
            priceMode = PriceMode.DISABLE;
            throw new IllegalArgumentException("Invalid price setting: " + config + ". The price will be disabled.");
        }
    }

    @Override
    public double getNewPrice() {
        return switch (priceMode) {
            case GAUSSIAN -> {
                Boolean round = getProperty(PriceProperty.ROUND);
                Double mean = getProperty(PriceProperty.MEAN);
                Double dev = getProperty(PriceProperty.DEV);
                if (round == null || mean == null || dev == null || Double.isNaN(mean) || Double.isNaN(dev))
                    throw new RuntimeException("Invalid price property: " + properties);
                double result = mean + dev * random.nextGaussian();
                if (result < 0) {
                    LogUtils.warn("A negative price was detected: " + properties + ". This price will be disabled for safety. Please check your price config.");
                    yield Double.NaN;
                }
                yield round ? Math.floor(result) : result;
            }
            case FIXED -> {
                Double fixed = getProperty(PriceProperty.FIXED);
                if (fixed == null) throw new RuntimeException("Invalid price property: " + properties);
                if (fixed < 0) {
                    LogUtils.warn("A negative price was detected: " + properties + ". This price will be disabled for safety. Please check your price config.");
                    yield Double.NaN;
                }
                yield fixed;
            }
            case MINMAX -> {
                Double min = getProperty(PriceProperty.MIN);
                Double max = getProperty(PriceProperty.MAX);
                Boolean round = getProperty(PriceProperty.ROUND);
                if (round == null || min == null || max == null || Double.isNaN(min) || Double.isNaN(max))
                    throw new RuntimeException("Invalid price property: " + properties);
                double result = min + (max - min) * random.nextDouble();
                if (result < 0) {
                    LogUtils.warn("A negative price was detected: " + properties + ". This price will be disabled for safety. Please check your price config.");
                    yield Double.NaN;
                }
                yield round ? Math.floor(result) : result;
            }
            default -> Double.NaN;
        };
    }

    @Override
    public @NotNull PriceMode getPriceMode() {
        return priceMode;
    }
}