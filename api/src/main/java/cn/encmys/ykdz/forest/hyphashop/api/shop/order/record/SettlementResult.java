package cn.encmys.ykdz.forest.hyphashop.api.shop.order.record;

import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record SettlementResult(@NotNull SettlementResultType type,
                               @NotNull Map<@NotNull String, @NotNull Double> prices) {
}
