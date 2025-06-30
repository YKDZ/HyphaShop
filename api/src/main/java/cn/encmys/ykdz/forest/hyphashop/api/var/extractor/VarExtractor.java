package cn.encmys.ykdz.forest.hyphashop.api.var.extractor;

import org.jetbrains.annotations.NotNull;

public interface VarExtractor {
    void extract(@NotNull VarInjectorContext ctx);
}
