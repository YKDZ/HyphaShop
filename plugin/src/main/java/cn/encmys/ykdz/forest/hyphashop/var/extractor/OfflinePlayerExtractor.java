package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class OfflinePlayerExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        final OfflinePlayer player = ctx.get(OfflinePlayer.class);
        if (player == null) return;

        ctx.putVar("__player", () -> player);
        ctx.putVar("player_name", player::getName);
        ctx.putVar("player_uuid", () -> player.getUniqueId().toString());

        ctx.putVar("cart_total_price", () -> HyphaShop.PROFILE_FACTORY.getProfile(player).getCart().getOrder().getTotalPrice());
        ctx.putVar("cart_shopping_mode", () -> HyphaShop.PROFILE_FACTORY.getProfile(player).getCart().getOrder().getType().name());
    }
}