package cn.encmys.ykdz.forest.hyphashop.api.currency.exception;

import org.jetbrains.annotations.NotNull;

public class CurrencyInitException extends Exception {
    public CurrencyInitException(@NotNull String message) {
        super(message);
    }
}
