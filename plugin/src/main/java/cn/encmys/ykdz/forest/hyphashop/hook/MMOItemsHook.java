package cn.encmys.ykdz.forest.hyphashop.hook;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import org.bukkit.Bukkit;

public class MMOItemsHook {
    public static void load() {
        if (isHooked()) {
            HyphaShopImpl.LOGGER.info("Hooked into MMOItems.");
        }
    }

    public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("MMOItems") != null;
    }
}
