package cn.encmys.ykdz.forest.hyphashop.api.product;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.product.enums.ProductType;
import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import cn.encmys.ykdz.forest.hyphashop.api.rarity.Rarity;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Product {
    protected final boolean isCacheable;
    private final @NotNull String id;
    private final @NotNull Price buyPrice;
    private final @NotNull Price sellPrice;
    private final @NotNull Rarity rarity;
    private final @NotNull BaseItemDecorator iconDecorator;
    private final @Nullable BaseItemDecorator itemDecorator;
    private final @NotNull ProductStock productStock;
    private final @NotNull ActionsConfig actions;
    private final @NotNull Context scriptContext;

    public Product(
            @NotNull String id,
            @NotNull Price buyPrice,
            @NotNull Price sellPrice,
            @NotNull Rarity rarity,
            @NotNull BaseItemDecorator iconDecorator,
            @Nullable BaseItemDecorator itemDecorator,
            @NotNull ProductStock productStock,
            @NotNull Context scriptContext,
            @NotNull ActionsConfig actions,
            boolean isCacheable) {
        this.id = id;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.rarity = rarity;
        this.iconDecorator = iconDecorator;
        this.itemDecorator = itemDecorator;
        this.productStock = productStock;
        this.scriptContext = scriptContext;
        this.actions = actions;
        this.isCacheable = isCacheable;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull Rarity getRarity() {
        return rarity;
    }

    public @NotNull Price getBuyPrice() {
        return buyPrice;
    }

    public @NotNull Price getSellPrice() {
        return sellPrice;
    }

    public abstract ProductType getType();

    @NotNull
    public BaseItemDecorator getIconDecorator() {
        return iconDecorator;
    }

    @Nullable
    public BaseItemDecorator getProductItemDecorator() {
        return itemDecorator;
    }

    public @NotNull ProductStock getProductStock() {
        return productStock;
    }

    public abstract void give(@NotNull Shop shop, @NotNull Player player, int stack);

    public abstract void give(@NotNull Shop shop, @NotNull Inventory inv, @NotNull Player player, int stack);

    public abstract void take(@NotNull Shop shop, @NotNull Player player, int stack);

    public abstract void take(@NotNull Shop shop, @NotNull Iterable<ItemStack> inv, @NotNull Player player, int stack);

    public abstract int has(@NotNull Shop shop, @NotNull Player player, int stack);

    public abstract int has(@NotNull Shop shop, @NotNull Iterable<ItemStack> inv, @NotNull Player player, int stack);

    public abstract boolean canHold(@NotNull Shop shop, @NotNull Player player, int stack);

    public abstract boolean isProductItemCacheable();

    public abstract boolean isMatch(@NotNull Shop shop, @NotNull ItemStack item, @NotNull Player player);

    public @NotNull Context getScriptContext() {
        return scriptContext;
    }

    public @NotNull ActionsConfig getActions() {
        return actions;
    }
}
