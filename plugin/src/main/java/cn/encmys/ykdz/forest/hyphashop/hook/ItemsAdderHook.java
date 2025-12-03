package cn.encmys.ykdz.forest.hyphashop.hook;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import org.bukkit.Bukkit;

public class ItemsAdderHook {
    public static void load() {
        if (isHooked()) {
            HyphaShopImpl.LOGGER.info("Hooked into ItemsAdder.");
        }
    }

    public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }
}
