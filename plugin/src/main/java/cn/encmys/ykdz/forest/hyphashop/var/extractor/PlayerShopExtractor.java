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

        final Player player = ctx.get(Player.class).orElse(null);
        final Shop shop = ctx.get(Shop.class).orElse(null);

        assert player != null;
        assert shop != null;

        ctx.putVar("cart_mode_id", () -> HyphaShop.PROFILE_FACTORY.getProfile(player).getCart().getOrder().getType().name().toLowerCase().replace("_", "-"));
        ctx.putVar("shopping_mode_id", () -> HyphaShop.PROFILE_FACTORY.getProfile(player).getShoppingMode(shop.getId()).name().toLowerCase().replace("_", "-"));
    }
}