package cn.encmys.ykdz.forest.hyphashop.config.record.shop;

import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.record.MerchantRecord;
import org.jetbrains.annotations.NotNull;

public record ShopSettingsRecord(int size, @NotNull String name,
                                 boolean autoRestockEnabled, long autoRestockPeriod,
                                 @NotNull MerchantRecord merchant, @NotNull String context,
                                 @NotNull ActionsConfig actions) {
}
