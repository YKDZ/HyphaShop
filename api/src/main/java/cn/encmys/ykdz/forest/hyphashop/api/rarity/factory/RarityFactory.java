package cn.encmys.ykdz.forest.hyphashop.api.rarity.factory;

import cn.encmys.ykdz.forest.hyphashop.api.rarity.Rarity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RarityFactory {
    void buildRarity(@NotNull String id, @NotNull String name, int weight);

    @Nullable Rarity getRarity(@NotNull String id);
}
