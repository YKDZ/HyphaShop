package cn.encmys.ykdz.forest.hyphashop.api.shop.counter;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public interface ShopCounter {
    void cacheAmount(@NotNull String productId);

    int getAmount(@NotNull String productId);

    @NotNull Shop getShop();

    @NotNull
    @Unmodifiable
    Map<String, Integer> getCachedAmounts();

    void setCachedAmounts(@NotNull Map<String, Integer> cachedAmounts);

    boolean hasCachedAmount(@NotNull String productId);
}
