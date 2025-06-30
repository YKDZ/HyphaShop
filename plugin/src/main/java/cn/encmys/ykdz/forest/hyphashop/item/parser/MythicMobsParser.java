package cn.encmys.ykdz.forest.hyphashop.item.parser;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.parser.BaseItemParser;
import cn.encmys.ykdz.forest.hyphashop.hook.MythicMobsHook;
import cn.encmys.ykdz.forest.hyphashop.item.MythicMobsItem;
import org.jetbrains.annotations.NotNull;

public class MythicMobsParser implements BaseItemParser {
    public static final @NotNull String PREFIX = "MM:";

    @Override
    public boolean canParse(@NotNull String base) {
        if (!MythicMobsHook.isHooked()) return false;
        if (!base.startsWith(PREFIX)) return false;

        String id = base.substring(PREFIX.length());
        BaseItem item = new MythicMobsItem(id);
        return item.isExist();
    }

    @Override
    public @NotNull BaseItem parse(@NotNull String base) {
        return new MythicMobsItem(base.substring(PREFIX.length()));
    }
}
