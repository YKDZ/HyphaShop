package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        final ItemStack item = ctx.get(ItemStack.class);
        if (item == null) return;

        ctx.putVar("__item", () -> item);
        ctx.putVar("item_type", () -> item.getType().name());
        ctx.putVar("item_amount", item::getAmount);
    }
}
