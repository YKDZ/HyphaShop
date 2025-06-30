package cn.encmys.ykdz.forest.hyphashop.item;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.enums.BaseItemType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaItem implements BaseItem {
    protected final @NotNull Material material;

    public VanillaItem(@NotNull Material material) {
        this.material = material;
    }

    @Override
    public @NotNull ItemStack build(@Nullable Player player) {
        return new ItemStack(getMaterial());
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull BaseItemDecorator decorator) {
        return Component.translatable(material.translationKey()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack item) {
        return getMaterial() == item.getType();
    }

    @Override
    public boolean isExist() {
        return true;
    }

    @Override
    public @NotNull BaseItemType getItemType() {
        return BaseItemType.VANILLA;
    }

    public @NotNull Material getMaterial() {
        return material;
    }
}
