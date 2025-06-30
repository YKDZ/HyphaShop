package cn.encmys.ykdz.forest.hyphashop.product.stock;

import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProductStockImpl implements ProductStock {
    private final @NotNull String productId;
    private final int initialGlobalAmount;
    private final @NotNull Map<@NotNull String, @NotNull Integer> currentPlayerAmount = new ConcurrentHashMap<>();
    private final int initialPlayerAmount;
    private final boolean globalReplenish;
    private final boolean playerReplenish;
    private final boolean globalOverflow;
    private final boolean playerOverflow;
    private final boolean globalInherit;
    private final boolean playerInherit;
    private final Object globalLock = new Object();
    private int currentGlobalAmount;

    public ProductStockImpl(@NotNull String productId, int initialGlobalAmount, int initialPlayerAmount, boolean globalReplenish, boolean playerReplenish, boolean globalOverflow, boolean playerOverflow, boolean globalInherit, boolean playerInherit) {
        this.productId = productId;
        this.initialGlobalAmount = initialGlobalAmount;
        this.currentGlobalAmount = initialGlobalAmount;
        this.initialPlayerAmount = initialPlayerAmount;
        this.globalReplenish = globalReplenish;
        this.playerReplenish = playerReplenish;
        this.globalOverflow = globalOverflow;
        this.playerOverflow = playerOverflow;
        this.globalInherit = globalInherit;
        this.playerInherit = playerInherit;
    }

    @Override
    public @NotNull String getProductId() {
        return productId;
    }

    @Override
    public int getCurrentGlobalAmount() {
        synchronized (globalLock) {
            return currentGlobalAmount;
        }
    }

    @Override
    public void setCurrentGlobalAmount(int currentGlobalAmount) {
        synchronized (globalLock) {
            this.currentGlobalAmount = currentGlobalAmount;
        }
    }

    @Override
    public void modifyGlobal(int amount) {
        if (!isGlobalStock()) return;
        synchronized (globalLock) {
            boolean isOverflow = isGlobalOverflow() && (currentGlobalAmount + amount) >= initialGlobalAmount;
            currentGlobalAmount = isOverflow ? initialGlobalAmount : currentGlobalAmount + amount;
        }
    }

    @Override
    public int getInitialPlayerAmount() {
        return initialPlayerAmount;
    }

    @Override
    public int getCurrentPlayerAmount(@NotNull UUID playerUUID) {
        return currentPlayerAmount.getOrDefault(playerUUID.toString(), initialPlayerAmount);
    }

    @Override
    public @NotNull @Unmodifiable Map<String, Integer> getCurrentPlayerAmount() {
        return Collections.unmodifiableMap(currentPlayerAmount);
    }

    @Override
    public void setCurrentPlayerAmount(@NotNull Map<String, Integer> amount) {
        this.currentPlayerAmount.clear();
        this.currentPlayerAmount.putAll(amount);
    }

    @Override
    public void setCurrentPlayerAmount(@NotNull String playerUUID, int amount) {
        this.currentPlayerAmount.put(playerUUID, amount);
    }

    @Override
    public int getInitialGlobalAmount() {
        return initialGlobalAmount;
    }

    @Override
    public boolean isPlayerReplenish() {
        return playerReplenish;
    }

    @Override
    public boolean isGlobalReplenish() {
        return globalReplenish;
    }

    @Override
    public boolean isPlayerOverflow() {
        return playerOverflow;
    }

    @Override
    public boolean isGlobalOverflow() {
        return globalOverflow;
    }

    @Override
    public boolean isPlayerStock() {
        return initialPlayerAmount != Integer.MIN_VALUE;
    }

    @Override
    public boolean isGlobalInherit() {
        return globalInherit;
    }

    @Override
    public boolean isPlayerInherit() {
        return playerInherit;
    }

    @Override
    public boolean isGlobalStock() {
        return initialGlobalAmount != Integer.MIN_VALUE;
    }

    @Override
    public boolean isStock() {
        return isGlobalStock() || isPlayerStock();
    }

    @Override
    public void stock() {
        if (!isStock()) return;

        if (isPlayerStock() && !isPlayerInherit()) {
            currentPlayerAmount.clear();
        }
        if (isGlobalStock() && !isGlobalInherit()) {
            synchronized (globalLock) {
                currentGlobalAmount = initialGlobalAmount;
            }
        }
    }

    @Override
    public void modifyPlayer(@NotNull UUID playerUUID, int amount) {
        if (!isPlayerStock()) return;

        final boolean isOverflow = isPlayerOverflow() && getCurrentPlayerAmount(playerUUID) + amount >= initialPlayerAmount;
        setCurrentPlayerAmount(playerUUID.toString(), isOverflow ? initialPlayerAmount : getCurrentPlayerAmount(playerUUID) + amount);
    }

    @Override
    public void modifyPlayer(@NotNull ShopOrder order) {
        final int stack = order.getOrderedProducts().entrySet().stream()
                .filter(entry -> entry.getKey().productId().equals(productId))
                .mapToInt(Map.Entry::getValue)
                .sum() * (order.getType() == OrderType.SELL_TO ? -1 : 1);
        modifyPlayer(order.getCustomerUUID(), stack);
    }

    @Override
    public void modifyGlobal(@NotNull ShopOrder order) {
        final int stack = order.getOrderedProducts().entrySet().stream()
                .filter(entry -> entry.getKey().productId().equals(productId))
                .mapToInt(Map.Entry::getValue)
                .sum() * (order.getType() == OrderType.SELL_TO ? -1 : 1);
        modifyGlobal(stack);
    }

    @Override
    public boolean isReachPlayerLimit(@NotNull UUID playerUUID, int stack) {
        if (!isPlayerStock()) return false;
        final int current = currentPlayerAmount.getOrDefault(playerUUID.toString(), initialPlayerAmount);
        return current - stack < 0;
    }

    @Override
    public boolean isReachGlobalLimit(int stack) {
        if (!isGlobalStock()) return false;
        synchronized (globalLock) {
            return currentGlobalAmount - stack < 0;
        }
    }
}
