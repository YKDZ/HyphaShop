package cn.encmys.ykdz.forest.hyphashop.item.parser;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.parser.BaseItemParser;
import cn.encmys.ykdz.forest.hyphashop.item.SkullItem;
import org.jetbrains.annotations.NotNull;

public class SkullParser implements BaseItemParser {
    private static final @NotNull String PREFIX = "SKULL:";

    @Override
    public boolean canParse(@NotNull String base) {
        if (!base.startsWith(PREFIX)) return false;
        BaseItem item = new SkullItem(base.substring(PREFIX.length()));
        return item.isExist();
    }

    @Override
    public @NotNull BaseItem parse(@NotNull String base) {
        return new SkullItem(base.substring(PREFIX.length()));
    }
}
