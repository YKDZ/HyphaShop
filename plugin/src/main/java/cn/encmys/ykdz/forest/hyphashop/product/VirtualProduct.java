package cn.encmys.ykdz.forest.hyphashop.product;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionEvent;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.product.enums.ProductType;
import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import cn.encmys.ykdz.forest.hyphashop.api.rarity.Rarity;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.utils.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 完全由 actions 控制行为的商品，既没有 item 设置，也没有 bundle-contents
 */
public class VirtualProduct extends Product {
    public VirtualProduct(
            @NotNull String id,
            @NotNull Price buyPrice,
            @NotNull Price sellPrice,
            @NotNull Rarity rarity,
            @NotNull BaseItemDecorator iconBuilder,
            @NotNull ProductStock productStock,
            @NotNull Context scriptContext,
            @NotNull ActionsConfig actions,
            boolean isCacheable) {
        super(id, buyPrice, sellPrice, rarity, iconBuilder, null, productStock, scriptContext, actions, isCacheable);
    }

    @Override
    public ProductType getType() {
        return ProductType.VIRTUAL;
    }

    @Override
    public void give(@NotNull Shop shop, @NotNull Player player, int stack) {
        give(shop, player.getInventory(), player, stack);
    }

    @Override
    public void give(@NotNull Shop shop, @NotNull Inventory inv, @NotNull Player player, int stack) {
        MiscUtils.processActions(ActionEvent.PRODUCT_ON_GIVE, getActions(), getScriptContext(), Map.of("stack", stack), shop, player, this);
    }

    @Override
    public void take(@NotNull Shop shop, @NotNull Player player, int stack) {
        take(shop, player.getInventory(), player, stack);
    }

    @Override
    public void take(@NotNull Shop shop, @NotNull Iterable<ItemStack> inv, @NotNull Player player, int stack) {
        MiscUtils.processActions(ActionEvent.PRODUCT_ON_TAKE, getActions(), getScriptContext(), Map.of("stack", stack), shop, player, this);
    }

    @Override
    public int has(@NotNull Shop shop, @NotNull Player player, int stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int has(@NotNull Shop shop, @NotNull Iterable<ItemStack> inv, @NotNull Player player, int stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canHold(@NotNull Shop shop, @NotNull Player player, int stack) {
        return true;
    }

    @Override
    public boolean isProductItemCacheable() {
        return false;
    }

    @Override
    public boolean isMatch(@NotNull Shop shop, @NotNull ItemStack item, @NotNull Player player) {
        return false;
    }
}
