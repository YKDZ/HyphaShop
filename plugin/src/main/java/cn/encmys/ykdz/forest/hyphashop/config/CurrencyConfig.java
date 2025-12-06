package cn.encmys.ykdz.forest.hyphashop.config;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurrencyConfig {
    private static final String path = HyphaShop.INSTANCE.getDataFolder() + "/currency.yml";
    private static final YamlConfiguration config = new YamlConfiguration();
    private static final Map<String, Double> exchangeRates = new HashMap<>();

    public static void load() {
        final File file = new File(path);

        if (!file.exists()) {
            HyphaShop.INSTANCE.saveResource("currency.yml", false);
        }

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException error) {
            HyphaShopImpl.LOGGER.error(error.getMessage());
        }

        exchangeRates.put(getBaseCurrency(), 1d);
        exchangeRates.putAll(parseExchangeRates());
    }

    public static @NotNull String getBaseCurrency() {
        return config.getString("base", "VAULT");
    }

    public static @NotNull Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }

    /**
     * 将给定 Map 中所有货币的金额总和转换为指定的目标货币
     *
     * @param from Map<货币 ID, 金额> - 包含要兑换的货币及其金额
     * @param to   目标货币 ID
     * @return 所有金额总和兑换成目标货币后的总金额
     */
    public static double exchange(@NotNull Map<String, Double> from, @NotNull String to) {
        if (!exchangeRates.containsKey(to)) {
            HyphaShopImpl.LOGGER.warn("""
                    Currency %s was used in product but not found in exchange rates config in currency.yml. You had better at least define a '%s: 1' in rates config for it. Plugin will use 1:1 as fallback.
                    """.formatted(to, to));
        }

        // 将所有金额标准化为基础货币的总和
        double totalAmountInBase = 0.0;

        for (Map.Entry<String, Double> entry : from.entrySet()) {
            final String currency = entry.getKey();
            final double amount = entry.getValue();

            // 获取待兑换货币相对于基础货币的汇率
            // 1 base = rate * currency -> 1 currency = 1 / rate * base
            // 将 amount * currency 转换为 base: amount / rate
            double rate = Optional.ofNullable(exchangeRates.get(currency)).orElseGet(() -> {
                HyphaShopImpl.LOGGER.warn("""
                        Currency %s was used in product but not found in exchange rates config in currency.yml. You had better at least define a '%s: 1' in rates config for it. Plugin will use 1:1 as fallback.
                        """.formatted(currency, currency));
                return 1d;
            });

            // amountInBase = amount / rate
            double amountInBase = amount / rate;

            totalAmountInBase += amountInBase;
        }

        // 将基础货币总和转换为目标货币
        // rateTo = 目标货币相对于基础货币的汇率 (1 base = rateTo * to)
        double rateTo = Optional.ofNullable(exchangeRates.get(to)).orElse(1d);

        return totalAmountInBase * rateTo;
    }

    private static @NotNull Map<String, Double> parseExchangeRates() {
        final ConfigurationSection section = config.getConfigurationSection("exchange-rates.rates");
        if (section == null) return Map.of();

        return section.getKeys(false).stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> section.getDouble(key, 1d)
                ));
    }
}
