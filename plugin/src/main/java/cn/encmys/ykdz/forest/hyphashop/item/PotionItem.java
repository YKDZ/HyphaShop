package cn.encmys.ykdz.forest.hyphashop.item;

import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public class PotionItem extends VanillaItem {
    public PotionItem(@NotNull Material material) {
        super(material);
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull BaseItemDecorator decorator) {
        final PotionType type = decorator.getProperty(ItemProperty.POTION_TYPE);
        return Component.translatable(material.translationKey(), type == null ? "" : ".effect." + type.name().toLowerCase().replaceAll("long_|strong_", "")).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
