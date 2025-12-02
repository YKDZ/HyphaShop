package cn.encmys.ykdz.forest.hyphashop.api.database.schema;

import cn.encmys.ykdz.forest.hyphashop.api.price.PricePair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record ShopSchema(@NotNull String id,
                         @NotNull Map<String, Double> balances,
                         @NotNull Map<String, Integer> cachedAmounts,
                         @NotNull Map<String, PricePair> cachedPrices,
                         @NotNull List<String> listedProducts,
                         long lastRestocking) {
    @Contract("_ -> new")
    public static @NotNull ShopSchema of(@NotNull Shop shop) {
        return new ShopSchema(shop.getId(),
                shop.getShopCashier().getBalances(),
                shop.getShopCounter().getCachedAmounts(),
                shop.getShopPricer().getCachedPrices(),
                shop.getShopStocker().getListedProducts(),
                shop.getShopStocker().getLastRestocking()
        );
    }
}
