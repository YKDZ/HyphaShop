package cn.encmys.ykdz.forest.hyphashop.utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    public static @NotNull BigDecimal round(double value, int scale) {
        return round(new BigDecimal(value), scale);
    }

    public static @NotNull BigDecimal round(@NotNull BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
