package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.record.ScriptOrComponentItemName;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.SettlementLogUtils;
import org.jetbrains.annotations.NotNull;

public class ProductExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        Product product = ctx.get(Product.class);
        if (product == null) return;

        ctx.putVar("__product", () -> product);
        ctx.putVar("product_id", product::getId);
        ctx.putVar("product_name", () -> {
            ScriptOrComponentItemName name = product.getIconDecorator().getNameOrUseBaseItemName();
            if (name.isScript()) return ScriptUtils.evaluateString(ctx.getTarget(), name.scriptName());
            else return name.componentName();
        });
        ctx.putVar("product_rarity_id", () -> product.getRarity().id());
        ctx.putVar("product_rarity_name", () -> product.getRarity().name());

        ctx.putVar("product_is_stock", () -> product.getProductStock().isStock());
        ctx.putVar("product_is_global_stock", () -> product.getProductStock().isGlobalStock());
        ctx.putVar("product_is_player_stock", () -> product.getProductStock().isStock());
        ctx.putVar("current_global_stock", () -> !product.getProductStock().isGlobalStock() ? null : product.getProductStock().getCurrentGlobalAmount());
        ctx.putVar("initial_global_stock", () -> !product.getProductStock().isGlobalStock() ? null : product.getProductStock().getInitialGlobalAmount());
        ctx.putVar("initial_player_stock", () -> !product.getProductStock().isPlayerStock() ? null : product.getProductStock().getInitialPlayerAmount());

        ctx.putVar("total_history_bought_amount", () -> SettlementLogUtils.getHistoryAmountFromLogs(
                product.getId(), OrderType.SELL_TO));
        ctx.putVar("total_history_sold_amount", () -> SettlementLogUtils.getHistoryAmountFromLogs(
                product.getId(), OrderType.BUY_FROM, OrderType.BUY_ALL_FROM));
        ctx.putVar("total_history_bought_stack", () -> SettlementLogUtils.getHistoryStackFromLogs(
                product.getId(), OrderType.SELL_TO));
        ctx.putVar("total_history_sold_stack", () -> SettlementLogUtils.getHistoryStackFromLogs(
                product.getId(), OrderType.BUY_FROM, OrderType.BUY_ALL_FROM));
    }
}
