package cn.encmys.ykdz.forest.hyphashop.item;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.enums.BaseItemType;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ItemsAdderItem(@NotNull String namespacedId) implements BaseItem {

    @Override
    public @NotNull Component getDisplayName(@NotNull BaseItemDecorator decorator) {
        final CustomStack instance = CustomStack.getInstance(namespacedId);
        if (instance == null)
            return Component.translatable("Name of ItemsAdder item " + namespacedId() + " not found").color(TextColor.color(255, 0, 0));
        return HyphaAdventureUtils.getComponentFromMiniMessage(HyphaAdventureUtils.legacyToMiniMessage(instance.getDisplayName()));
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack item) {
        final CustomStack target = CustomStack.byItemStack(item);
        if (target == null) return false;
        return target.getNamespacedID().equals(namespacedId);
    }

    @Override
    public boolean isExist() {
        return CustomStack.isInRegistry(namespacedId);
    }

    @Override
    public @NotNull BaseItemType getItemType() {
        return BaseItemType.ITEMS_ADDER;
    }

    @Override
    public @NotNull ItemStack build(@Nullable Player player) {
        final CustomStack stack = CustomStack.getInstance(namespacedId());
        if (stack != null) {
            return stack.getItemStack().clone();
        } else {
            return new ItemStack(Material.AIR);
        }
    }
}
