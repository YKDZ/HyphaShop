package cn.encmys.ykdz.forest.hyphashop.shop.cashier;

import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.ShopCashier;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.record.MerchantRecord;
import org.jetbrains.annotations.NotNull;

public class ShopCashierImpl implements ShopCashier {
    private final @NotNull Shop shop;
    private final double initBalance;
    private final boolean replenish;
    private final boolean overflow;
    private final boolean inherit;
    private double balance;

    public ShopCashierImpl(@NotNull Shop shop, @NotNull MerchantRecord merchant) {
        this.shop = shop;
        this.initBalance = merchant.initBalance();
        this.balance = initBalance;
        this.replenish = merchant.replenish();
        this.overflow = merchant.overflow();
        this.inherit = merchant.inherit();
    }

    @Override
    public void modifyBalance(double value) {
        if (!isMerchant()) return;

        final double newValue = balance + value;
        balance = (newValue > initBalance) && !overflow ? initBalance : newValue;
    }

    @Override
    public double getInitBalance() {
        return initBalance;
    }

    @Override
    public boolean isReplenish() {
        return replenish;
    }

    @Override
    public boolean isOverflow() {
        return overflow;
    }

    @Override
    public boolean isInherit() {
        return inherit;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public boolean isMerchant() {
        return !Double.isNaN(initBalance);
    }

    @Override
    public void restockMerchant() {
        if (isMerchant() && !inherit) {
            balance = initBalance;
        }
    }

    @Override
    public @NotNull Shop getShop() {
        return shop;
    }
}
