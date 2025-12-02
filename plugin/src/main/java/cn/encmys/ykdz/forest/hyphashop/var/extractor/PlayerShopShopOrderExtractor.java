package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerShopShopOrderExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        if (!ctx.hasAll(ShopOrder.class, Player.class, Shop.class)) return;

        final Player player = ctx.get(Player.class).orElse(null);
        final Shop shop = ctx.get(Shop.class).orElse(null);
        final ShopOrder order = ctx.get(ShopOrder.class).orElse(null);

        assert player != null;
        assert shop != null;
        assert order != null;

        ctx.putVar("order_total_price", order::getTotalPrices);
    }
}
