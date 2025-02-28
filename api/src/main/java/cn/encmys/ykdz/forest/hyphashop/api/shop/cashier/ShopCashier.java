package cn.encmys.ykdz.forest.hyphashop.api.shop.cashier;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResult;
import org.jetbrains.annotations.NotNull;

/**
 * Check and Checkout the ShopOrder.
 */
public interface ShopCashier {
    /**
     * Calculate the prices of all products in given ShopOrder and store them (ignore given totalStack value).
     * Each ShopOrder can only be billed once.
     * <p>
     * Orders of type BUY_ALL_FROM are unlikely to get the billing value before being settled.
     * @param order ShopOrder that will be calculated
     */
    void billOrder(@NotNull ShopOrder order);

    /**
     * Checkout and finish the given ShopOrder for the customer.
     * Each ShopOrder can only be settled once.
     * @param order ShopOrder that will be settled
     * @return SettlementResult of this settle action
     */
    @NotNull SettlementResult settle(@NotNull ShopOrder order);

    @NotNull SettlementResult canSellTo(@NotNull ShopOrder order);

    @NotNull SettlementResult canBuyFrom(@NotNull ShopOrder order);

    boolean canHold(@NotNull ShopOrder order);

    void logSettlement(@NotNull ShopOrder order);

    void modifyBalance(double value);

    double getInitBalance();

    boolean isSupply();

    boolean isOverflow();

    boolean isInherit();

    double getBalance();

    /**
     * This method is only used to initialize the balance value and does not respect the supply and overflow settings.
     * You should use modifyBalance to change the balance value.
     */
    void setBalance(double balance);

    boolean isMerchant();

    void restockMerchant();

    Shop getShop();
}
