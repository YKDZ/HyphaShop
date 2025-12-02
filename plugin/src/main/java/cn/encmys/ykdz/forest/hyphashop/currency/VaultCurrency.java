package cn.encmys.ykdz.forest.hyphashop.currency;

import cn.encmys.ykdz.forest.hyphashop.api.currency.CurrencyProvider;
import cn.encmys.ykdz.forest.hyphashop.api.currency.exception.CurrencyInitException;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class VaultCurrency implements CurrencyProvider {
    private static @Nullable Economy economy;

    @Override
    public @NotNull String getId() {
        return "VAULT";
    }

    public void init(@NotNull Plugin instance) throws CurrencyInitException {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            throw new CurrencyInitException("Vault not found!");
        }
        final RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new CurrencyInitException("No Vault supported economy provider plugin (XConomy or CMI etc.) found!");
        }
        economy = rsp.getProvider();
    }

    public boolean deposit(@NotNull Player player, @Positive double amount) {
        return getEconomy().map(economy -> economy.depositPlayer(player, amount).transactionSuccess()).orElse(false);
    }

    public boolean withdraw(@NotNull Player player, @Positive double amount) {
        return getEconomy().map(economy -> economy.withdrawPlayer(player, amount).transactionSuccess()).orElse(false);
    }

    public double getBalance(@NotNull Player player) {
        return getEconomy().map(economy -> economy.getBalance(player)).orElse(0d);
    }

    private @NotNull Optional<Economy> getEconomy() {
        return Optional.ofNullable(economy);
    }
}
