package cn.encmys.ykdz.forest.hyphashop.currency;

import cn.encmys.ykdz.forest.hyphashop.api.currency.CurrencyProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

public class ExpCurrency implements CurrencyProvider {
    @Override
    public @NotNull String getId() {
        return "EXP";
    }

    public void init(@NotNull Plugin instance) {
    }

    public boolean deposit(@NotNull OfflinePlayer offlinePlayer, @Positive double amount) {
        final Player player = offlinePlayer.getPlayer();
        if (player == null) return false;

        player.giveExp((int) amount, false);
        return true;
    }

    public boolean withdraw(@NotNull OfflinePlayer offlinePlayer, @Positive double amount) {
        final Player player = offlinePlayer.getPlayer();
        if (player == null) return false;

        player.giveExp((int) -amount, false);
        return true;
    }

    public double getBalance(@NotNull OfflinePlayer offlinePlayer) {
        final Player player = offlinePlayer.getPlayer();
        if (player == null) return 0d;

        return player.getTotalExperience();
    }
}
