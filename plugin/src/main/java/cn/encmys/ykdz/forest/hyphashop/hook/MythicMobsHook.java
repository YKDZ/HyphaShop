package cn.encmys.ykdz.forest.hyphashop.hook;

import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import org.bukkit.Bukkit;

public class MythicMobsHook {

    public static void load() {
        if (isHooked()) {
            LogUtils.info("Hooked into MythicMobs.");
        }
    }

    public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }
}
