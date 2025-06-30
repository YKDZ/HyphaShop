package cn.encmys.ykdz.forest.hyphashop.shop.counter;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.counter.ShopCounter;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShopCounterImpl implements ShopCounter {
    private final @NotNull Shop shop;
    private @NotNull Map<@NotNull String, @NotNull Integer> cachedAmounts = new HashMap<>();

    public ShopCounterImpl(@NotNull Shop shop) {
        this.shop = shop;
    }

    @Override
    public void cacheAmount(@NotNull String productId) {
        final Product product = HyphaShop.PRODUCT_FACTORY.getProduct(productId);
        if (product == null) {
            LogUtils.warn("Try to cache amount for product " + productId + " which does not exist.");
            return;
        }

        // 优先缓存 ItemDecorator 的数量
        // 否则缓存 IconDecorator
        final BaseItemDecorator targetDecorator = product.getProductItemDecorator() != null ?
                product.getProductItemDecorator() :
                product.getIconDecorator();
        final Script amountConfig = targetDecorator.getProperty(ItemProperty.AMOUNT);

        if (amountConfig == null) cachedAmounts.put(productId, 1);
        else {
            // product -> shop -> global
            final Context ctx = ContextUtils.linkContext(
                    product.getScriptContext().clone(),
                    shop.getScriptContext().clone()
            );
            cachedAmounts.put(productId, ScriptUtils.evaluateInt(ctx, amountConfig));
        }
    }

    @Override
    public int getAmount(@NotNull String productId) {
        if (!cachedAmounts.containsKey(productId)) {
            LogUtils.warn("Try to get amount for product " + productId + " which does not be cached. The amount will fallback to 1. This could be a plugin issue.");
            try {
                throw new RuntimeException();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
        return cachedAmounts.get(productId);
    }

    @Override
    public @NotNull Shop getShop() {
        return shop;
    }

    @Override
    public @NotNull @Unmodifiable Map<@NotNull String, @NotNull Integer> getCachedAmounts() {
        return Collections.unmodifiableMap(cachedAmounts);
    }

    @Override
    public void setCachedAmounts(@NotNull Map<@NotNull String, @NotNull Integer> cachedAmounts) {
        this.cachedAmounts = cachedAmounts;
    }

    @Override
    public boolean hasCachedAmount(@NotNull String productId) {
        return cachedAmounts.containsKey(productId);
    }
}
