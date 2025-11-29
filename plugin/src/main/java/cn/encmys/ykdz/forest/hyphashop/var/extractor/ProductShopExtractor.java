package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.jetbrains.annotations.NotNull;

public class ProductShopExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        if (!ctx.hasAll(Product.class, Shop.class)) return;

        final Product product = ctx.get(Product.class).orElse(null);
        final Shop shop = ctx.get(Shop.class).orElse(null);

        assert shop != null;
        assert product != null;

        if (shop.getShopCounter().hasCachedAmount(product.getId())) {
            ctx.putVar("product_amount", () -> shop.getShopCounter().getAmount(product.getId()));
        }

        if (shop.getShopPricer().hasCachedPrice(product.getId())) {
            ctx.putVar("buy_price", () -> Double.isNaN(shop.getShopPricer().getBuyPrice(product.getId())) ? null : shop.getShopPricer().getBuyPrice(product.getId()));
            ctx.putVar("sell_price", () -> Double.isNaN(shop.getShopPricer().getSellPrice(product.getId())) ? null : shop.getShopPricer().getSellPrice(product.getId()));
        }

        if (shop.isProductItemCached(product.getId())) {
            ctx.putVar("__product_item", () -> shop.getCachedProductItem(product));
        }
    }
}