package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.Item;

public class IconExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        Item icon = ctx.get(Item.class);
        if (icon == null) return;

        ctx.putVar("__icon", () -> icon);
    }
}
