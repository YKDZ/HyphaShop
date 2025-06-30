package cn.encmys.ykdz.forest.hyphashop.item.parser;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.parser.BaseItemParser;
import cn.encmys.ykdz.forest.hyphashop.hook.ItemsAdderHook;
import cn.encmys.ykdz.forest.hyphashop.item.ItemsAdderItem;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderParser implements BaseItemParser {
    private static final @NotNull String PREFIX = "IA:";

    @Override
    public boolean canParse(@NotNull String base) {
        if (!ItemsAdderHook.isHooked()) return false;
        if (!base.startsWith(PREFIX)) return false;

        final BaseItem item = new ItemsAdderItem(base.substring(PREFIX.length()));
        return item.isExist();
    }

    @Override
    public @NotNull BaseItem parse(@NotNull String base) {
        return new ItemsAdderItem(base.substring(PREFIX.length()));
    }
}
