package cn.encmys.ykdz.forest.hyphashop.api.database.dao;

import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface SettlementLogDao {
    @NotNull List<SettlementLog> queryLogs(@NotNull String shopId, @NotNull OrderType @NotNull ... types);

    @NotNull List<SettlementLog> queryLogs(@NotNull UUID playerUUID, int offset, int limit, @NotNull OrderType @NotNull ... types);

    int queryHistoryStack(@NotNull String productId, @NotNull OrderType @NotNull ... types);

    int queryHistoryAmount(@NotNull String productId, @NotNull OrderType @NotNull ... types);

    void insertLog(@NotNull SettlementLog log);

    void deleteLog(@NotNull UUID customerUUID, long daysLateThan);

    int countLog(@NotNull UUID customerUUID, @NotNull OrderType @NotNull ... types);
}
