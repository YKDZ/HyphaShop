package cn.encmys.ykdz.forest.hyphashop.api.event.shop;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ShopPreSettleEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Shop shop;
    private final ShopOrder order;
    private boolean isCancelled = false;

    public ShopPreSettleEvent(@NotNull Shop shop, @NotNull ShopOrder order) {
        this.shop = shop;
        this.order = order;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    public Shop getShop() {
        return shop;
    }

    @NotNull
    public ShopOrder getOrder() {
        return order;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
