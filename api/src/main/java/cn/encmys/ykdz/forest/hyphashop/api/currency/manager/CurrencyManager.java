package cn.encmys.ykdz.forest.hyphashop.api.currency.manager;

import cn.encmys.ykdz.forest.hyphashop.api.currency.CurrencyProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Optional;

public interface CurrencyManager {
    void load();

    @NotNull
    @Unmodifiable
    Map<String, CurrencyProvider> getProviders();

    void register(@NotNull CurrencyProvider provider);

    @NotNull Optional<CurrencyProvider> getCurrency(@NotNull String id);
}
