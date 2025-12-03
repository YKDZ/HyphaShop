package cn.encmys.ykdz.forest.hyphashop.hook;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import org.bukkit.Bukkit;

public class PlaceholderAPIHook {
    public static void load() {
        if (isHooked()) {
            (new PlaceholderExpansion()).register();
            HyphaShopImpl.LOGGER.info("Hooked into PlaceholderAPI.");
        }
    }

    public static boolean isHooked() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
}
