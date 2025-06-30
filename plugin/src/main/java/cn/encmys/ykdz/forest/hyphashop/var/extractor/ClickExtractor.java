package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;

public class ClickExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        Click click = ctx.get(Click.class);
        if (click == null) return;

        ctx.putVar("__click", () -> click);
        ctx.putVar("click_type_id", () -> click.clickType().name());
        ctx.putVar("click_slot_type_id", () -> click.clickType().name());
    }
}
