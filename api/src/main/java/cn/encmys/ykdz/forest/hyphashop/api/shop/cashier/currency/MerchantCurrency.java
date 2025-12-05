package cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.currency;

import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.record.MerchantRecord;
import org.jetbrains.annotations.NotNull;

public class MerchantCurrency {
    private final double initBalance;
    private final boolean replenish;
    private final boolean overflow;
    private final boolean inherit;
    private double balance;

    public MerchantCurrency(@NotNull MerchantRecord merchant) {
        this.initBalance = merchant.initBalance();
        this.balance = this.initBalance;
        this.replenish = merchant.replenish();
        this.overflow = merchant.overflow();
        this.inherit = merchant.inherit();
    }

    public void modifyBalance(double value) {
        final double newValue = balance + value;
        if (newValue < 0) throw new RuntimeException("""
                Shop merchant currency can not handle this price. This is a bug. Please report it.
                """);
        this.balance = (newValue > initBalance) && !overflow ? initBalance : newValue;
    }

    public double getInitBalance() {
        return initBalance;
    }

    public boolean isReplenish() {
        return replenish;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public boolean isInherit() {
        return inherit;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = Math.min(balance, initBalance);
    }

    public void restock() {
        if (!isInherit()) this.balance = initBalance;
    }
}
