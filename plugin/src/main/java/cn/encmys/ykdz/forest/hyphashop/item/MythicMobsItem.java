package cn.encmys.ykdz.forest.hyphashop.item;

import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.enums.BaseItemType;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MythicMobsItem implements BaseItem {
    private final @NotNull String id;
    private @Nullable MythicBukkit mythicBukkit;

    public MythicMobsItem(@NotNull String id) {
        this.id = id;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull BaseItemDecorator decorator) {
        if (mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        for (MythicItem item : mythicBukkit.getItemManager().getItems()) {
            if (item.getInternalName().equals(getId())) {
                return HyphaAdventureUtils.getComponentFromMiniMessage(HyphaAdventureUtils.legacyToMiniMessage(item.getDisplayName()));
            }
        }
        return Component.translatable("Name of MythicMobs item " + getId() + " not found").color(TextColor.color(255, 0, 0));
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack item) {
        if (mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        return mythicBukkit.getItemManager().isMythicItem(item) && mythicBukkit.getItemManager().getMythicTypeFromItem(item).equals(getId());
    }

    @Override
    public boolean isExist() {
        if (mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        return mythicBukkit.getItemManager().getItem(id).isPresent();
    }

    @Override
    public @NotNull BaseItemType getItemType() {
        return BaseItemType.MYTHIC_MOBS;
    }

    @Override
    public @NotNull ItemStack build(@Nullable Player player) {
        if (mythicBukkit == null || mythicBukkit.isClosed()) {
            this.mythicBukkit = MythicBukkit.inst();
        }
        return mythicBukkit.getItemManager().getItemStack(id);
    }

    public @NotNull String getId() {
        return id;
    }
}
