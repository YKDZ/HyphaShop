package cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log;

import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public interface SettlementLog {
    @NotNull OrderType getType();

    @NotNull SettlementLog setType(@NotNull OrderType type);

    @NotNull Date getTransitionTime();

    @NotNull SettlementLog setTransitionTime(@NotNull Date transitionTime);

    double getTotalPrice();

    @NotNull UUID getCustomerUUID();

    @NotNull SettlementLog setCustomerUUID(@NotNull UUID customerUUID);

    @NotNull
    @Unmodifiable
    Map<ProductLocation, AmountPair> getOrderedProducts();

    @NotNull SettlementLog setOrderedProducts(@NotNull Map<ProductLocation, AmountPair> orderedProducts);

    @NotNull
    @Unmodifiable
    Map<ProductLocation, Double> getBill();

    @NotNull SettlementLog setBill(@NotNull Map<ProductLocation, Double> bill);
}
