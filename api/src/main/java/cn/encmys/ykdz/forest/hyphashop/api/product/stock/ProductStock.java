package cn.encmys.ykdz.forest.hyphashop.api.product.stock;

import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface ProductStock {
    @NotNull String getProductId();

    int getCurrentGlobalAmount();

    void setCurrentGlobalAmount(int currentGlobalAmount);

    int getInitialPlayerAmount();

    int getCurrentPlayerAmount(@NotNull UUID uuid);

    @NotNull Map<String, Integer> getCurrentPlayerAmount();

    void setCurrentPlayerAmount(@NotNull Map<String, Integer> amount);

    void setCurrentPlayerAmount(@NotNull String uuid, int amount);

    int getInitialGlobalAmount();

    boolean isPlayerReplenish();

    boolean isGlobalReplenish();

    boolean isStock();

    void stock();

    void modifyPlayer(@NotNull UUID uuid, int amount);

    void modifyGlobal(int amount);

    boolean isPlayerStock();

    boolean isGlobalInherit();

    boolean isPlayerInherit();

    boolean isGlobalStock();

    boolean isPlayerOverflow();

    boolean isGlobalOverflow();

    void modifyPlayer(@NotNull ShopOrder shopOrder);

    void modifyGlobal(@NotNull ShopOrder order);

    boolean isReachPlayerLimit(@NotNull UUID playerUUID, int stack);

    boolean isReachGlobalLimit(int stack);
}
