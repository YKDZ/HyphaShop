package cn.encmys.ykdz.forest.hyphashop.api.profile.cart;

import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Cart {
    @NotNull ShopOrder getOrder();

    void setOrder(@NotNull ShopOrder order);

    @NotNull UUID getOwnerUUID();
}
