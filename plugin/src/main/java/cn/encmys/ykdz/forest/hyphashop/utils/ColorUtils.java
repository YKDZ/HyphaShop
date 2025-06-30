package cn.encmys.ykdz.forest.hyphashop.utils;

import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class ColorUtils {
    public static @NotNull Color getFromHex(@NotNull String hex) {
        int color = Integer.parseInt(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return Color.fromRGB(r, g, b);
    }
}
