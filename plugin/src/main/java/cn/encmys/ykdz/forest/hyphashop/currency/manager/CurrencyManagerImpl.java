package cn.encmys.ykdz.forest.hyphashop.currency.manager;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.currency.CurrencyProvider;
import cn.encmys.ykdz.forest.hyphashop.api.currency.exception.CurrencyInitException;
import cn.encmys.ykdz.forest.hyphashop.api.currency.manager.CurrencyManager;
import cn.encmys.ykdz.forest.hyphashop.currency.ExpCurrency;
import cn.encmys.ykdz.forest.hyphashop.currency.VaultCurrency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CurrencyManagerImpl implements CurrencyManager {
    public static final @NotNull CurrencyProvider VAULT_CURRENCY = new VaultCurrency();

    private final @NotNull Map<@NotNull String, @NotNull CurrencyProvider> providers = new HashMap<>();
    
    public CurrencyManagerImpl() {
        load();
    }

    @Override
    public void load() {
        register(new ExpCurrency());
        register(new VaultCurrency());
    }

    @Override
    public @NotNull @Unmodifiable Map<String, CurrencyProvider> getProviders() {
        return Collections.unmodifiableMap(providers);
    }

    @Override
    public void register(@NotNull CurrencyProvider provider) {
        try {
            provider.init(HyphaShop.INSTANCE);
        } catch (CurrencyInitException e) {
            HyphaShopImpl.LOGGER.error("""
                    Error when init currency provider %s, this currency will not available in HyphaShop.
                    """.formatted(provider.getId()));
        }
        providers.put(provider.getId(), provider);
    }

    @Override
    public @NotNull Optional<CurrencyProvider> getCurrency(@NotNull String id) {
        return Optional.ofNullable(providers.get(id));
    }
}
