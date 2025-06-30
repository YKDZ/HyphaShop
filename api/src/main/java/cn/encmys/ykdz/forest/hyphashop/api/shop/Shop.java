package cn.encmys.ykdz.forest.hyphashop.api.shop;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.gui.GUI;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.ShopCashier;
import cn.encmys.ykdz.forest.hyphashop.api.shop.counter.ShopCounter;
import cn.encmys.ykdz.forest.hyphashop.api.shop.pricer.ShopPricer;
import cn.encmys.ykdz.forest.hyphashop.api.shop.stocker.ShopStocker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public interface Shop {
    @NotNull String getName();

    @NotNull String getId();

    @NotNull GUI getShopGUI();

    @NotNull
    @Unmodifiable
    Map<String, ItemStack> getCachedProductItems();

    boolean isProductItemCached(@NotNull String productId);

    void cacheProductItem(@NotNull Product product);

    @Nullable ItemStack getCachedProductItem(@NotNull Product product);

    @NotNull ItemStack getCachedProductItemOrBuildOne(@NotNull Product product, Player player);

    @NotNull ShopPricer getShopPricer();

    @NotNull ShopCashier getShopCashier();

    @NotNull ShopStocker getShopStocker();

    @NotNull ShopCounter getShopCounter();

    @NotNull Context getScriptContext();

    @NotNull ActionsConfig getActions();
}
