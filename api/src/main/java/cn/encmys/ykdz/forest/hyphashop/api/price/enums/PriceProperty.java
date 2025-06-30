package cn.encmys.ykdz.forest.hyphashop.api.price.enums;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

public enum PriceProperty {
    FIXED(new TypeToken<Double>() {
    }),
    MEAN(new TypeToken<Double>() {
    }),
    DEV(new TypeToken<Double>() {
    }),
    MIN(new TypeToken<Double>() {
    }),
    MAX(new TypeToken<Double>() {
    }),
    ROUND(new TypeToken<Boolean>() {
    }),
    CONTEXT(new TypeToken<Context>() {
    }),
    FORMULA(new TypeToken<Script>() {
    });

    @NotNull
    private final TypeToken<?> token;

    PriceProperty(@NotNull TypeToken<?> token) {
        this.token = token;
    }

    public @NotNull TypeToken<?> getToken() {
        return token;
    }
}
