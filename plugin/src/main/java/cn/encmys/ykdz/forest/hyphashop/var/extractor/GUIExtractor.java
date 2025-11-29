package cn.encmys.ykdz.forest.hyphashop.var.extractor;

import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarExtractor;
import cn.encmys.ykdz.forest.hyphashop.api.var.extractor.VarInjectorContext;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.ScrollGui;

public class GUIExtractor implements VarExtractor {
    @Override
    public void extract(@NotNull VarInjectorContext ctx) {
        final Gui gui = ctx.get(Gui.class).orElse(null);
        if (gui == null) return;

        ctx.putVar("__gui", () -> gui);
        ctx.putVar("current_line", () -> {
            if (gui instanceof ScrollGui<?>) {
                return ((ScrollGui<?>) gui).getLine();
            }
            return null;
        });
        ctx.putVar("max_line", () -> {
            if (gui instanceof ScrollGui<?>) {
                return ((ScrollGui<?>) gui).getMaxLine();
            }
            return null;
        });
        ctx.putVar("current_page", () -> {
            if (gui instanceof PagedGui<?>) {
                return ((PagedGui<?>) gui).getPage();
            }
            return null;
        });
        ctx.putVar("total_page", () -> {
            if (gui instanceof PagedGui<?>) {
                return ((PagedGui<?>) gui).getPageCount();
            }
            return null;
        });
    }
}
