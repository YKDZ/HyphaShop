package cn.encmys.ykdz.forest.hyphashop.api.price;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public interface PriceInstance {
    @NotNull Map<String, Double> getPrices();

    @NotNull Optional<Double> getPrice(@NotNull String id);

    boolean isEmpty();

    @NotNull Map<@NotNull String, @NotNull Double> mul(double stack);

    @NotNull PriceInstance merge(@NotNull Map<String, Double> target);

    boolean withdraw(@NotNull OfflinePlayer customer);

    boolean deposit(@NotNull OfflinePlayer customer);
}
