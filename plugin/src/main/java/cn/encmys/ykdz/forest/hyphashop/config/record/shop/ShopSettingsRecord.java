package cn.encmys.ykdz.forest.hyphashop.config.record.shop;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.record.MerchantRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ShopSettingsRecord(int size, @NotNull Script name,
                                 boolean autoRestockEnabled, long autoRestockPeriod,
                                 @NotNull Map<@NotNull String, @NotNull MerchantRecord> merchant,
                                 @NotNull String context,
                                 @NotNull ActionsConfig actions) {
}
