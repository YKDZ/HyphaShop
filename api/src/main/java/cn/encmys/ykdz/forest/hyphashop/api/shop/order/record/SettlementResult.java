package cn.encmys.ykdz.forest.hyphashop.api.shop.order.record;

import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import org.jetbrains.annotations.NotNull;

public record SettlementResult(@NotNull SettlementResultType type, double price) {
}
