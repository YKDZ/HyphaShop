package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

public class ShopExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        Shop shop = ctx.get(Shop.class);
        if (shop == null) return;

        ctx.putVar("__shop", () -> shop);
        ctx.putVar("shop_name", shop::getName);
        ctx.putVar("shop_id", shop::getId);

        boolean isMerchant = shop.getShopCashier().isMerchant();

        ctx.putVar("is_merchant", () -> shop.getShopCashier().isMerchant());
        if (isMerchant) {
            ctx.putVar("merchant_balance", () -> shop.getShopCashier().getBalance());
        }

        ctx.putVar("__gui_structure", () -> shop.getShopGUI().getStructure().toArray());

        ctx.putVar("restock_timer", () -> TextUtils.parseRestockTimer(shop));
    }
}