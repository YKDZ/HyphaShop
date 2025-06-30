package cn.encmys.ykdz.forest.hyphashop.profile;

import cn.encmys.ykdz.forest.hyphashop.api.gui.GUI;
import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.profile.cart.Cart;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.config.CartGUIConfig;
import cn.encmys.ykdz.forest.hyphashop.config.OrderHistoryGUIConfig;
import cn.encmys.ykdz.forest.hyphashop.gui.CartGUI;
import cn.encmys.ykdz.forest.hyphashop.gui.OrderHistoryGUI;
import cn.encmys.ykdz.forest.hyphashop.profile.cart.CartImpl;
import cn.encmys.ykdz.forest.hyphashop.shop.order.ShopOrderImpl;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;

public class ProfileImpl implements Profile {
    private final @NotNull OfflinePlayer owner;
    private final @NotNull Cart cart;
    private final @NotNull CartGUI cartGUI;
    private final @NotNull OrderHistoryGUI orderHistoryGUI;
    private @NotNull Map<@NotNull String, @NotNull ShoppingMode> shoppingModes = new HashMap<>();
    private @Nullable Window viewingWindow;

    public ProfileImpl(@NotNull OfflinePlayer owner) {
        this.owner = owner;
        this.cartGUI = new CartGUI(owner, CartGUIConfig.getGUIConfig());
        this.orderHistoryGUI = new OrderHistoryGUI(owner, OrderHistoryGUIConfig.getGUIConfig());
        this.cart = new CartImpl(owner.getUniqueId(), new ShopOrderImpl(owner.getUniqueId()));
    }

    @Override
    public @NotNull OfflinePlayer getOwner() {
        return owner;
    }

    @Override
    public @NotNull Map<String, ShoppingMode> getShoppingModes() {
        return shoppingModes;
    }

    @Override
    public void setShoppingModes(@NotNull Map<String, ShoppingMode> shoppingModes) {
        this.shoppingModes = shoppingModes;
    }

    @Override
    public @NotNull ShoppingMode getShoppingMode(@NotNull String shopId) {
        return shoppingModes.getOrDefault(shopId, ShoppingMode.DIRECT);
    }

    @Override
    public void setShoppingMode(@NotNull String shopId, @NotNull ShoppingMode shoppingMode) {
        shoppingModes.put(shopId, shoppingMode);
        cart.getOrder().setBilled(false);
    }

    @Override
    public @NotNull Cart getCart() {
        return cart;
    }

    @Override
    public @NotNull GUI getCartGUI() {
        return cartGUI;
    }

    @Override
    public @NotNull GUI getOrderHistoryGUI() {
        return orderHistoryGUI;
    }

    @Override
    public @Nullable Window getViewingWindow() {
        return viewingWindow;
    }

    @Override
    public void setViewingWindow(@Nullable Window viewingWindow) {
        this.viewingWindow = viewingWindow;
    }
}
