package cn.encmys.ykdz.forest.hyphashop.utils;

import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

public class ColorUtils {
    public static @NotNull Color getFromHex(@NotNull String hex) {
        final int color = Integer.parseInt(hex.replace("#", ""), 16);
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = color & 0xFF;
        return Color.fromRGB(r, g, b);
    }
}
