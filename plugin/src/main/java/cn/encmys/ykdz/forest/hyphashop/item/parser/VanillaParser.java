package cn.encmys.ykdz.forest.hyphashop.item.parser;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.parser.BaseItemParser;
import cn.encmys.ykdz.forest.hyphashop.item.VanillaItem;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class VanillaParser implements BaseItemParser {
    private static final @NotNull Registry<@NotNull Material> materialRegistry = Registry.MATERIAL;

    @Override
    public boolean canParse(@NotNull String base) {
        try {
            materialRegistry.getOrThrow(Key.key(base));
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    @Override
    public @NotNull BaseItem parse(@NotNull String base) {
        try {
            final Material material = materialRegistry.getOrThrow(Key.key(base));
            return new VanillaItem(material);
        } catch (NoSuchElementException | InvalidKeyException e) {
            return new VanillaItem(Material.DIRT);
        }
    }
}
