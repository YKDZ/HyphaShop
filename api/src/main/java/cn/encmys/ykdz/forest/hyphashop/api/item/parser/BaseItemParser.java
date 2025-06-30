package cn.encmys.ykdz.forest.hyphashop.api.item.parser;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import org.jetbrains.annotations.NotNull;

public interface BaseItemParser {
    boolean canParse(@NotNull String base);

    @NotNull BaseItem parse(@NotNull String base);
}
