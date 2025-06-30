package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public class PlayerUtils {
    public static boolean canHold(@NotNull Player player, @NotNull Shop shop, @NotNull Product product, @NotNull AmountPair amountPair) {
        ItemStack item = shop.getCachedProductItemOrBuildOne(product, player);

        int neededSpace = amountPair.totalAmount();
        for (ItemStack checked : player.getInventory().getStorageContents()) {
            if (checked == null || checked.getType().isAir()) {
                neededSpace -= item.getMaxStackSize();
            } else if (checked.isSimilar(item)) {
                neededSpace -= item.getMaxStackSize() - checked.getAmount();
            }
            if (neededSpace <= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean canHold(@NotNull Player player, @NotNull Shop shop, @NotNull Map<Product, AmountPair> productsToHold) {
        Inventory inv = player.getInventory();
        // 空白格子的总数
        int emptySlots = (int) Arrays.stream(inv.getStorageContents())
                .filter(item -> item == null || item.getType().isAir())
                .count();

        int totalRequiredSlots = 0;

        for (Map.Entry<Product, AmountPair> entry : productsToHold.entrySet()) {
            Product product = entry.getKey();
            int totalAmount = entry.getValue().totalAmount();

            if (totalAmount <= 0) continue;

            ItemStack productItem = shop.getCachedProductItemOrBuildOne(product, player);
            int maxStack = productItem.getMaxStackSize();

            // 可以堆叠入现存物品的数量
            int stackableSpace = Arrays.stream(inv.getStorageContents())
                    .filter(item -> item != null
                            // 保证原理上确实可以堆叠
                            // 可以不检查 isSimilar
                            && productItem.isSimilar(item))
                    .mapToInt(item -> (maxStack - item.getAmount()))
                    .sum();

            int remaining = totalAmount - stackableSpace;

            if (remaining <= 0) continue;

            // 剩下的物品需要占用的空白格子数量
            int slotsNeeded = (int) Math.ceil((double) remaining / maxStack);
            totalRequiredSlots += slotsNeeded;

            // 若空白格子不足，则无法装下
            if (totalRequiredSlots > emptySlots) return false;
        }

        return true;
    }
}
