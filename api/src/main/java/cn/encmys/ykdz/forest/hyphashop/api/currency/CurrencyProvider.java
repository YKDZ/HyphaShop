package cn.encmys.ykdz.forest.hyphashop.api.currency;

import cn.encmys.ykdz.forest.hyphashop.api.currency.exception.CurrencyInitException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

public interface CurrencyProvider {
    @NotNull String getId();

    void init(@NotNull Plugin instance) throws CurrencyInitException;

    boolean deposit(@NotNull Player player, @Positive double amount);

    boolean withdraw(@NotNull Player player, @Positive double amount);

    double getBalance(@NotNull Player player);
}
