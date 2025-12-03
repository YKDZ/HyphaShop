package cn.encmys.ykdz.forest.hyphashop.hook;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import org.bukkit.Bukkit;

public class MythicMobsHook {

    public static void load() {
        if (isHooked()) {
            HyphaShopImpl.LOGGER.info("Hooked into MythicMobs.");
        }
    }

    public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }
}
