package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.jetbrains.annotations.NotNull;

public class ShopOrderExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        final ShopOrder order = ctx.get(ShopOrder.class).orElse(null);
        if (order == null) return;

        ctx.putVar("__order", () -> order);
        ctx.putVar("order_total_price", order::getTotalPrices);
        ctx.putVar("order_is_billed", order::isBilled);
    }
}
