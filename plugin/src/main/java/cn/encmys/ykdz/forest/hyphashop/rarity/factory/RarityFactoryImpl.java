package cn.encmys.ykdz.forest.hyphashop.rarity.factory;

import cn.encmys.ykdz.forest.hyphashop.api.rarity.Rarity;
import cn.encmys.ykdz.forest.hyphashop.api.rarity.factory.RarityFactory;
import cn.encmys.ykdz.forest.hyphashop.config.RarityConfig;
import cn.encmys.ykdz.forest.hyphashop.rarity.RarityImpl;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class RarityFactoryImpl implements RarityFactory {
    @NotNull
    private static final HashMap<String, Rarity> rarities = new HashMap<>();

    public RarityFactoryImpl() {
        load();
    }

    public void load() {
        YamlConfiguration config = RarityConfig.getConfig();
        for (String id : RarityConfig.getAllId()) {
            buildRarity(
                    id,
                    config.getString("rarities." + id + ".name", "<red>Rarity name not found."),
                    config.getInt("rarities." + id + ".weight", 0)
            );
        }
    }

    @Override
    public void buildRarity(@NotNull String id, @NotNull String name, int weight) {
        rarities.put(id, new RarityImpl(id, name, weight));
    }

    @Override
    public @Nullable Rarity getRarity(@NotNull String id) {
        return rarities.get(id);
    }
}
