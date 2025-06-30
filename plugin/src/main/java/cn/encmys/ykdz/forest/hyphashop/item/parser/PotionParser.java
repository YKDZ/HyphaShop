package cn.encmys.ykdz.forest.hyphashop.item.parser;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.parser.BaseItemParser;
import cn.encmys.ykdz.forest.hyphashop.item.PotionItem;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

public class PotionParser implements BaseItemParser {
    private static final @NotNull Registry<@NotNull Material> materialRegistry = Registry.MATERIAL;

    @Override
    public boolean canParse(@NotNull String base) {
        try {
            final Material material = materialRegistry.getOrThrow(Key.key(base));
            return material.name().contains("POTION");
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public @NotNull BaseItem parse(@NotNull String base) {
        try {
            final Material material = materialRegistry.getOrThrow(Key.key(base));
            return new PotionItem(material);
        } catch (Exception ignored) {
            return new PotionItem(Material.POTION);
        }
    }
}
