package cn.encmys.ykdz.forest.hyphashop.api.shop.order;

import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.SettlementResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.UUID;

public interface ShopOrder {
    void combineOrder(@NotNull ShopOrder order);

    @NotNull ShopOrder modifyStack(@NotNull ProductLocation productLoc, int amount);

    @NotNull ShopOrder setStack(@NotNull ProductLocation productLoc, int amount);

    @NotNull OrderType getType();

    @NotNull ShopOrder setType(@NotNull OrderType orderType);

    void bill();

    @NotNull SettlementResult settle();

    @NotNull SettlementResultType canSellTo();

    @NotNull SettlementResultType canBuyFrom();

    boolean canHold();

    @NotNull UUID getCustomerUUID();

    @NotNull ShopOrder setCustomerUUID(@NotNull UUID customerUUID);

    @NotNull
    @Unmodifiable
    Map<ProductLocation, Integer> getOrderedProducts();

    @NotNull ShopOrder setOrderedProducts(@NotNull Map<ProductLocation, Integer> orderedProducts);

    double getBilledPrice(@NotNull ProductLocation productLoc);

    @NotNull ShopOrder setBill(@NotNull @Unmodifiable Map<ProductLocation, Double> bill);

    double getTotalPrice();

    boolean isBilled();

    @NotNull ShopOrder setBilled(boolean billed);

    void clear();

    void clean();

    @NotNull ShopOrder clone();
}
