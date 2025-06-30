package cn.encmys.ykdz.forest.hyphashop.api.item;

import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.enums.BaseItemType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BaseItem {
    @NotNull BaseItemType getItemType();

    @NotNull Component getDisplayName(@NotNull BaseItemDecorator decorator);

    @NotNull ItemStack build(@Nullable Player player);

    boolean isSimilar(@NotNull ItemStack item);

    boolean isExist();
}
