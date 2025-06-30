package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.jetbrains.annotations.NotNull;

public class SettlementLogExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        SettlementLog log = ctx.get(SettlementLog.class);
        if (log == null) return;

        ctx.putVar("__log", () -> log);
        ctx.putVar("log_date", log::getTransitionTime);
        ctx.putVar("log_type_id", () -> log.getType().name());
        ctx.putVar("log_total_price", log::getTotalPrice);
    }
}
