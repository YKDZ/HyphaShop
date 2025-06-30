package cn.encmys.ykdz.forest.hyphashop.api.event.shop;

import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This event is triggered when the store restock is started (not yet finished).
 */
public class ShopPreRestockEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Shop shop;
    private final List<Product> productsPreparedToBeListed;
    private boolean isCancelled = false;

    public ShopPreRestockEvent(Shop shop, List<Product> productsPreparedToBeListed) {
        this.shop = shop;
        this.productsPreparedToBeListed = productsPreparedToBeListed;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Shop getShop() {
        return shop;
    }

    public List<Product> getProductsPreparedToBeListed() {
        return productsPreparedToBeListed;
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
