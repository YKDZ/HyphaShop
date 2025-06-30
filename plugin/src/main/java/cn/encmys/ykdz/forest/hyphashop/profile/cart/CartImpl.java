package cn.encmys.ykdz.forest.hyphashop.profile.cart;

import cn.encmys.ykdz.forest.hyphashop.api.profile.cart.Cart;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CartImpl implements Cart {
    private final @NotNull UUID ownerUUID;
    private @NotNull ShopOrder order;

    public CartImpl(@NotNull UUID ownerUUID, @NotNull ShopOrder order) {
        this.ownerUUID = ownerUUID;
        this.order = order;
    }

    @Override
    public @NotNull ShopOrder getOrder() {
        return order;
    }

    @Override
    public void setOrder(@NotNull ShopOrder order) {
        this.order = order;
    }

    @Override
    public @NotNull UUID getOwnerUUID() {
        return ownerUUID;
    }
}
