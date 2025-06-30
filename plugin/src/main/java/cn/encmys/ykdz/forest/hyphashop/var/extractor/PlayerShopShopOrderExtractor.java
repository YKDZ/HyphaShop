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

        Player player = ctx.get(Player.class);
        Shop shop = ctx.get(Shop.class);
        ShopOrder order = ctx.get(ShopOrder.class);

        assert player != null;
        assert shop != null;
        assert order != null;

        ctx.putVar("order_total_price", order::getTotalPrice);
    }
}
