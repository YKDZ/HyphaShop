package cn.encmys.ykdz.forest.hyphashop.script.pack;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import org.jetbrains.annotations.NotNull;

@ObjectName("Server")
public class ServerObject extends InternalObject {
    @Static
    @Function("broadcast")
    @FunctionParas("msg")
    public static void broadcast(@NotNull Context ctx) {
        String msg = ContextUtils.getStringParam(ctx, "msg").orElse(null);
        if (msg == null) return;

        HyphaShop.INSTANCE.getServer().broadcast(HyphaAdventureUtils.getComponentFromMiniMessage(msg));
    }
}
