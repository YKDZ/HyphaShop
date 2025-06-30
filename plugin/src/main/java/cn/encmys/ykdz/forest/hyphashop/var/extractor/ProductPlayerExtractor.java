package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProductPlayerExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        if (!ctx.hasAll(Product.class, Player.class)) return;

        final Product product = ctx.get(Product.class);
        final Player player = ctx.get(Player.class);

        assert product != null;
        assert player != null;

        ctx.putVar("current_player_stock", () -> !product.getProductStock().isPlayerStock() ? null : product.getProductStock().getCurrentPlayerAmount(player.getUniqueId()));
    }
}
