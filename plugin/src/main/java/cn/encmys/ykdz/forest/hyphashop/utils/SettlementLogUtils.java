package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import org.jetbrains.annotations.NotNull;

public class SettlementLogUtils {
    public static long getHistoryAmountFromLogs(@NotNull String productId, @NotNull OrderType... types) {
        return HyphaShop.DATABASE_FACTORY.getSettlementLogDao().queryHistoryAmount(productId, types);
    }

    public static long getHistoryStackFromLogs(@NotNull String productId, @NotNull OrderType... types) {
        return HyphaShop.DATABASE_FACTORY.getSettlementLogDao().queryHistoryStack(productId, types);
    }
}
