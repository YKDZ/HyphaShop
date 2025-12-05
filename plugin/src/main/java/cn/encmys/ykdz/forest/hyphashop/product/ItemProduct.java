package cn.encmys.ykdz.forest.hyphashop.product;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionEvent;
import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.enums.BaseItemType;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.product.enums.ProductType;
import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import cn.encmys.ykdz.forest.hyphashop.api.rarity.Rarity;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.utils.MiscUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ItemProduct extends Product {
    public ItemProduct(
            @NotNull String id,
            @NotNull List<Price> buyPrice,
            @NotNull List<Price> sellPrice,
            @NotNull Rarity rarity,
            @NotNull BaseItemDecorator iconBuilder,
            @NotNull BaseItemDecorator productItemBuilder,
            @NotNull ProductStock productStock,
            @NotNull Context scriptContext,
            @NotNull ActionsConfig actions,
            boolean isCacheable) {
        super(id, buyPrice, sellPrice, rarity, iconBuilder, productItemBuilder, productStock, scriptContext, actions, isCacheable);
    }

    @Override
    public ProductType getType() {
        return ProductType.ITEM;
    }

    @Override
    public void give(@NotNull Shop shop, @NotNull Player player, int stack) {
        give(shop, player.getInventory(), player, stack);
    }

    @Override
    public void give(@NotNull Shop shop, @NotNull Inventory inv, @NotNull Player player, int stack) {
        MiscUtils.processActions(ActionEvent.PRODUCT_ON_GIVE, getActions(), getScriptContext(), Map.of("stack", stack), shop, player, this);

        final ItemStack item = shop.getCachedProductItemOrBuildOne(this, player);
        // 缓存中物品的 getAmount 结果永远是 1
        IntStream.range(0, stack * shop.getShopCounter().getAmount(getId())).forEach(i -> inv.addItem(item));
    }

    @Override
    public void take(@NotNull Shop shop, @NotNull Player player, int stack) {
        take(shop, player.getInventory(), player, stack);
    }

    @Override
    public void take(@NotNull Shop shop, @NotNull Iterable<ItemStack> inv, @NotNull Player player, int stack) {
        MiscUtils.processActions(ActionEvent.PRODUCT_ON_TAKE, getActions(), getScriptContext(), Map.of("stack", stack), shop, player, this);

        final BaseItemDecorator decorator = getProductItemDecorator();
        if (decorator == null) {
            return;
        }

        int needed = shop.getShopCounter().getAmount(getId()) * stack;
        if (has(shop, inv, player, 1) < stack) {
            return;
        }

        for (ItemStack check : inv) {
            if (check != null && needed > 0 && isMatch(shop, check, player)) {
                final int has = check.getAmount();
                if (needed <= has) {
                    check.setAmount(has - needed);
                    needed = 0;
                } else {
                    check.setAmount(0);
                    needed -= has;
                }
            }
        }
    }

    @Override
    public int has(@NotNull Shop shop, @NotNull Player player, int stack) {
        return has(shop, player.getInventory(), player, stack);
    }

    @Override
    public int has(@NotNull Shop shop, @NotNull Iterable<ItemStack> inv, @NotNull Player player, int stack) {
        final BaseItemDecorator decorator = getProductItemDecorator();
        if (decorator == null) {
            return 0;
        }

        int total = 0;
        final int stackedAmount = shop.getShopCounter().getAmount(getId()) * stack;
        for (final ItemStack check : inv) {
            if (check != null && isMatch(shop, check, player)) {
                total += check.getAmount();
            }
        }
        return total / stackedAmount;
    }

    @Override
    public boolean canHold(@NotNull Shop shop, @NotNull Player player, int stack) {
        return PlayerUtils.canHold(player, shop, this, new AmountPair(shop.getShopCounter().getAmount(getId()), stack));
    }

    @Override
    public boolean isProductItemCacheable() {
        return isCacheable && getType() == ProductType.ITEM;
    }

    @Override
    public boolean isMatch(@NotNull Shop shop, @NotNull ItemStack item, @NotNull Player player) {
        // 只要 actions 中有一个结果为 false
        // 则认为不匹配并提前返回
        final List<Value> result = MiscUtils.processActionsWithResult(ActionEvent.PRODUCT_ON_MATCH, getActions(), getScriptContext(), Collections.emptyMap(), shop, player, this);
        if (result.stream().anyMatch(value -> !value.getAsBoolean())) return false;

        final BaseItemDecorator decorator = getProductItemDecorator();
        if (decorator == null) return false;

        final BaseItem baseItem = decorator.getBaseItem();

        if (baseItem.getItemType() != BaseItemType.VANILLA) {
            return baseItem.isSimilar(item);
        } else {
            final ItemStack target = shop.getCachedProductItemOrBuildOne(this, player);
            return baseItem.isSimilar(item) && target.isSimilar(item);
        }
    }
}
