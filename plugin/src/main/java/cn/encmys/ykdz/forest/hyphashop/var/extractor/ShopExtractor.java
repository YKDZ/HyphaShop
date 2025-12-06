package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import org.jetbrains.annotations.NotNull;

public class ShopExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        final Shop shop = ctx.get(Shop.class).orElse(null);
        if (shop == null) return;

        ctx.putVar("__shop", () -> shop);
        ctx.putVar("shop_name", () -> ScriptUtils.evaluateString(ctx.getTarget(), shop.getName()));
        ctx.putVar("shop_id", shop::getId);

        final boolean isMerchant = shop.getShopCashier().isMerchant();

        ctx.putVar("is_merchant", () -> isMerchant);
        if (isMerchant)
            ctx.putVar("merchant_balances", () -> shop.getShopCashier().getBalances());

        ctx.putVar("__gui_structure", () -> shop.getShopGUI().getStructure().toArray());
        ctx.putVar("restock_time", () -> shop.getMillisUntilRestock(System.currentTimeMillis()));
    }
}