package cn.encmys.ykdz.forest.hyphashop.api.profile;

import cn.encmys.ykdz.forest.hyphashop.api.gui.GUI;
import cn.encmys.ykdz.forest.hyphashop.api.profile.cart.Cart;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.xenondevs.invui.window.Window;

import java.util.Map;
import java.util.Optional;

public interface Profile {
    @NotNull OfflinePlayer getOwner();

    @Unmodifiable
    @NotNull Map<String, ShoppingMode> getShoppingModes();

    void setShoppingModes(@NotNull Map<String, ShoppingMode> shoppingModes);

    @NotNull ShoppingMode getShoppingMode(@NotNull String shopId);

    void setShoppingMode(@NotNull String shopId, @NotNull ShoppingMode shoppingMode);

    @NotNull Cart getCart();

    @NotNull GUI getCartGUI();

    @NotNull GUI getOrderHistoryGUI();

    @NotNull Optional<Window> getViewingWindow();

    void setViewingWindow(@Nullable Window viewingWindow);

    @NotNull Optional<GUI> getPreviousGUI();

    void setPreviousGUI(@Nullable GUI previousGUI);
}
