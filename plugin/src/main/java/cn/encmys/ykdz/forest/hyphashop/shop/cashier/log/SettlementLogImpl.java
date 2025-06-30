package cn.encmys.ykdz.forest.hyphashop.shop.cashier.log;

import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class SettlementLogImpl implements SettlementLog {
    protected final @NotNull Map<ProductLocation, AmountPair> orderedProducts = new HashMap<>();
    protected final @NotNull Map<ProductLocation, Double> bill = new HashMap<>();
    protected @NotNull UUID customer;
    protected @NotNull OrderType type;
    protected @NotNull Date transitionTime = new Date();

    public SettlementLogImpl(@NotNull UUID customer, @NotNull OrderType type) {
        this.customer = customer;
        this.type = type;
    }

    @Override
    public @NotNull SettlementLog setCustomerUUID(@NotNull UUID customerUUID) {
        this.customer = customerUUID;
        return this;
    }

    @Override
    public @NotNull SettlementLog setType(@NotNull OrderType type) {
        this.type = type;
        return this;
    }

    @Override
    public @NotNull SettlementLog setTransitionTime(@NotNull Date transitionTime) {
        this.transitionTime = transitionTime;
        return this;
    }

    @Override
    public @NotNull @Unmodifiable Map<ProductLocation, AmountPair> getOrderedProducts() {
        return Collections.unmodifiableMap(orderedProducts);
    }

    @Override
    public @NotNull SettlementLog setOrderedProducts(@NotNull Map<ProductLocation, AmountPair> orderedProducts) {
        this.orderedProducts.clear();
        this.orderedProducts.putAll(orderedProducts);
        return this;
    }

    @Override
    public @NotNull SettlementLog setBill(@NotNull Map<ProductLocation, Double> bill) {
        this.bill.clear();
        this.bill.putAll(bill);
        return this;
    }

    @Override
    public @NotNull @Unmodifiable Map<ProductLocation, Double> getBill() {
        return Collections.unmodifiableMap(bill);
    }

    @Override
    public @NotNull Date getTransitionTime() {
        return transitionTime;
    }

    @Override
    public @NotNull OrderType getType() {
        return type;
    }

    @Override
    public @NotNull UUID getCustomerUUID() {
        return customer;
    }

    @Override
    public double getTotalPrice() {
        return bill.values().stream().reduce(0.0, Double::sum);
    }
}
