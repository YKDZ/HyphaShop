package cn.encmys.ykdz.forest.hyphashop.api.shop.cashier;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;

/**
 * Check and Checkout the ShopOrder.
 */
public interface ShopCashier {
    void modifyBalance(double value);

    double getInitBalance();

    boolean isReplenish();

    boolean isOverflow();

    boolean isInherit();

    double getBalance();

    /**
     * This method is only used to initialize the balance value and does not respect the replenish and overflow settings.
     * You should use modifyBalance to change the balance value.
     */
    void setBalance(double balance);

    boolean isMerchant();

    void restockMerchant();

    Shop getShop();
}
