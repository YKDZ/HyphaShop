package cn.encmys.ykdz.forest.hyphashop.script.pack;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphashop.utils.ContextUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ObjectName("PlaceholderAPI")
public class PlaceholderAPIObject extends InternalObject {
    @Static
    @Function("papi")
    @FunctionParas({"str", "__player"})
    public static String papi(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        String str = ContextUtils.getStringParam(ctx, "str").orElse("");

        return PlaceholderAPI.setPlaceholders(player, str);
    }
}
