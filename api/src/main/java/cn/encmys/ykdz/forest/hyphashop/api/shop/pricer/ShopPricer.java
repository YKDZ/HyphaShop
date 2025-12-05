package cn.encmys.ykdz.forest.hyphashop.api.shop.pricer;

import cn.encmys.ykdz.forest.hyphashop.api.price.PriceInstance;
import cn.encmys.ykdz.forest.hyphashop.api.price.PricePair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ShopPricer {
    @NotNull PriceInstance getBuyPrice(@NotNull String productId);

    @NotNull PriceInstance getSellPrice(@NotNull String productId);

    boolean cachePrice(@NotNull String productId);

    boolean hasCachedPrice(@NotNull String productId);

    // TODO 打折功能入口
    PricePair getModifiedPricePair(@NotNull String productId, @NotNull PricePair pricePair);

    Map<String, PricePair> getCachedPrices();

    void setCachedPrices(@NotNull Map<String, PricePair> cachedPrices);

    Shop getShop();
}
