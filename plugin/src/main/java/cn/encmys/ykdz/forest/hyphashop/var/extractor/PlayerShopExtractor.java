package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerShopExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        if (!ctx.hasAll(Player.class, Shop.class)) return;

        Player player = ctx.get(Player.class);
        Shop shop = ctx.get(Shop.class);

        assert player != null;
        assert shop != null;

        ctx.putVar("cart_mode_id", () -> HyphaShop.PROFILE_FACTORY.getProfile(player).getCart().getOrder().getType().name());
        ctx.putVar("shopping_mode_id", () -> HyphaShop.PROFILE_FACTORY.getProfile(player).getShoppingMode(shop.getId()).name());
    }
}