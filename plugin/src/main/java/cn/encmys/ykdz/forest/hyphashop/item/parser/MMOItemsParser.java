package cn.encmys.ykdz.forest.hyphashop.item.parser;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.parser.BaseItemParser;
import cn.encmys.ykdz.forest.hyphashop.hook.MMOItemsHook;
import cn.encmys.ykdz.forest.hyphashop.item.MMOItemsItem;
import org.jetbrains.annotations.NotNull;

public class MMOItemsParser implements BaseItemParser {
    private static final @NotNull String PREFIX = "MI:";

    @Override
    public boolean canParse(@NotNull String base) {
        if (!MMOItemsHook.isHooked()) return false;
        if (!base.startsWith(PREFIX)) return false;

        try {
            String[] typeId = base.substring(PREFIX.length()).split(":");
            BaseItem item = new MMOItemsItem(typeId[0], typeId[1]);
            if (!item.isExist()) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }

        return true;
    }

    @Override
    public @NotNull BaseItem parse(@NotNull String base) {
        String[] typeId = base.substring(PREFIX.length()).split(":");
        return new MMOItemsItem(typeId[0], typeId[1]);
    }
}
