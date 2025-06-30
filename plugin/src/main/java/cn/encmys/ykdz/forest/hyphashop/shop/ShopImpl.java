package cn.encmys.ykdz.forest.hyphashop.shop;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.ShopCashier;
import cn.encmys.ykdz.forest.hyphashop.api.shop.counter.ShopCounter;
import cn.encmys.ykdz.forest.hyphashop.api.shop.pricer.ShopPricer;
import cn.encmys.ykdz.forest.hyphashop.api.shop.stocker.ShopStocker;
import cn.encmys.ykdz.forest.hyphashop.config.ShopConfig;
import cn.encmys.ykdz.forest.hyphashop.config.record.shop.ShopSettingsRecord;
import cn.encmys.ykdz.forest.hyphashop.gui.ShopGUI;
import cn.encmys.ykdz.forest.hyphashop.item.builder.ProductItemBuilder;
import cn.encmys.ykdz.forest.hyphashop.shop.cashier.ShopCashierImpl;
import cn.encmys.ykdz.forest.hyphashop.shop.counter.ShopCounterImpl;
import cn.encmys.ykdz.forest.hyphashop.shop.pricer.ShopPricerImpl;
import cn.encmys.ykdz.forest.hyphashop.shop.stocker.ShopStockerImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class ShopImpl implements Shop {
    private final @NotNull String id;
    private final @NotNull String name;
    private final @NotNull ShopGUI shopGUI;
    private final @NotNull ShopPricer shopPricer;
    private final @NotNull ShopCashier shopCashier;
    private final @NotNull ShopStocker shopStocker;
    private final @NotNull ShopCounter shopCounter;
    private final @NotNull Map<String, ItemStack> cachedProductItems = new HashMap<>();
    private final @NotNull Context scriptContext;
    private final @NotNull ActionsConfig actions;

    public ShopImpl(@NotNull String id, @NotNull ShopSettingsRecord settings, @NotNull List<String> allProductsId) {
        this.id = id;
        this.name = settings.name();
        this.shopGUI = new ShopGUI(ShopConfig.getShopGUIConfig(id), this);
        this.shopPricer = new ShopPricerImpl(this);
        this.shopCashier = new ShopCashierImpl(this, settings.merchant());
        this.shopStocker = new ShopStockerImpl(this, settings.size(), settings.autoRestockEnabled(), settings.autoRestockPeriod(), allProductsId);
        this.shopCounter = new ShopCounterImpl(this);
        this.scriptContext = ScriptUtils.extractContext(settings.context());
        this.actions = settings.actions();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull ShopGUI getShopGUI() {
        return shopGUI;
    }

    @Override
    public boolean isProductItemCached(@NotNull String productId) {
        return cachedProductItems.containsKey(productId);
    }

    @Override
    public void cacheProductItem(@NotNull Product product) {
        if (product.getProductItemDecorator() == null) {
            throw new RuntimeException("Check Product#isCacheable before Shop#cacheProductItem");
        }
        if (product.isProductItemCacheable()) {
            cachedProductItems.put(product.getId(), ProductItemBuilder.build(this, product, null));
        }
    }

    @Override
    public @Nullable ItemStack getCachedProductItem(@NotNull Product product) {
        final String id = product.getId();
        if (product.isProductItemCacheable() && !isProductItemCached(id)) {
            cacheProductItem(product);
        }
        return cachedProductItems.get(id);
    }

    @Override
    public @NotNull ItemStack getCachedProductItemOrBuildOne(@NotNull Product product, Player player) {
        if (product.getProductItemDecorator() == null) {
            throw new RuntimeException("Check Product#isCacheable before Shop#getCachedProductItemOrCreateOne");
        }
        return Optional.ofNullable(getCachedProductItem(product))
                .orElse(ProductItemBuilder.build(this, product, player));
    }

    @Override
    public @NotNull ShopPricer getShopPricer() {
        return shopPricer;
    }

    @Override
    public @NotNull ShopCashier getShopCashier() {
        return shopCashier;
    }

    @Override
    public @NotNull ShopStocker getShopStocker() {
        return shopStocker;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, ItemStack> getCachedProductItems() {
        return Collections.unmodifiableMap(cachedProductItems);
    }

    @Override
    public @NotNull ShopCounter getShopCounter() {
        return shopCounter;
    }

    @Override
    public @NotNull Context getScriptContext() {
        return scriptContext;
    }

    @Override
    public @NotNull ActionsConfig getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return "ShopImpl{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shopGUI=" + shopGUI +
                ", shopPricer=" + shopPricer +
                ", shopCashier=" + shopCashier +
                ", shopStocker=" + shopStocker +
                ", shopCounter=" + shopCounter +
                ", cachedProductItems=" + cachedProductItems +
                ", scriptContext=" + scriptContext +
                '}';
    }
}
