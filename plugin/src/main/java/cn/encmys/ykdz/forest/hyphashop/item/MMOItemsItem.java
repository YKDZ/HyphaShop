package cn.encmys.ykdz.forest.hyphashop.item;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.enums.BaseItemType;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.NameData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public record MMOItemsItem(@NotNull Type type, @NotNull String id) implements BaseItem {
    public MMOItemsItem(@NotNull String type, @NotNull String id) {
        this(Objects.requireNonNull(Type.get(type)), id);
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull BaseItemDecorator decorator) {
        final MMOItem mmoItem = MMOItems.plugin.getMMOItem(type(), id().toUpperCase(Locale.ENGLISH));

        if (mmoItem == null) {
            return Component.translatable("Name of MMOItems item " + type().getId() + ":" + id() + " not found").color(TextColor.color(255, 0, 0));
        }

        for (var stat : mmoItem.getStats()) {
            if (stat.getId().equals("NAME")) {
                final String name = HyphaAdventureUtils.legacyToMiniMessage(((NameData) mmoItem.getData(stat)).getMainName());
                return HyphaAdventureUtils.getComponentFromMiniMessage(name);
            }
        }

        return Component.translatable("Name of MMOItems item " + id() + " not found").color(TextColor.color(255, 0, 0));
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack item) {
        final Type itemType = Type.get(MMOItems.getTypeName(item));
        final String itemId = MMOItems.getID(item);
        if (itemType != null && itemId != null) {
            return itemType.equals(type()) && itemId.equals(id());
        } else {
            return false;
        }
    }

    @Override
    public boolean isExist() {
        return MMOItems.plugin.getMMOItem(type, id) != null;
    }

    @Override
    public @NotNull BaseItemType getItemType() {
        return BaseItemType.MMOITEMS;
    }

    @Override
    public @NotNull ItemStack build(@Nullable Player player) {
        if (Bukkit.isPrimaryThread()) {
            return buildSync(player);
        } else {
            final CompletableFuture<ItemStack> future = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {
                try {
                    future.complete(buildSync(player));
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                HyphaShopImpl.LOGGER.warn(e.getMessage());
                return new ItemStack(Material.AIR);
            }
        }
    }

    private @NotNull ItemStack buildSync(@Nullable Player player) {
        final MMOItem mmoItem;
        if (player == null) {
            mmoItem = MMOItems.plugin.getMMOItem(type(), id().toUpperCase(Locale.ENGLISH));
        } else {
            mmoItem = MMOItems.plugin.getMMOItem(type(), id().toUpperCase(Locale.ENGLISH), PlayerData.get(player));
        }
        return mmoItem == null ? new ItemStack(Material.AIR) : Objects.requireNonNull(mmoItem.newBuilder().build());
    }
}
