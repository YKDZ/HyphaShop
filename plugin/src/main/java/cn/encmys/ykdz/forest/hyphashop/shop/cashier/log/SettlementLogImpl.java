package cn.encmys.ykdz.forest.hyphashop.shop.cashier.log;

import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

public class SettlementLogImpl implements SettlementLog {
    protected final @NotNull Map<ProductLocation, AmountPair> orderedProducts = new HashMap<>();
    protected final @NotNull Map<ProductLocation, Double> pricePerStack = new HashMap<>();
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
    public @NotNull SettlementLog setPricePerStack(@NotNull Map<ProductLocation, Double> pricePerStack) {
        this.pricePerStack.clear();
        this.pricePerStack.putAll(pricePerStack);
        return this;
    }

    @Override
    public @NotNull @Unmodifiable Map<ProductLocation, Double> getPricePerStack() {
        return Collections.unmodifiableMap(pricePerStack);
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
    public @NotNull Map<@NotNull String, @NotNull Double> getTotalPrices() {
        return orderedProducts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> {
                            final Product product = entry.getKey().product();
                            if (product == null) throw new RuntimeException("Product is null");

                            if (type == OrderType.SELL_TO) {
                                return product.getBuyPrice().getCurrencyProvider().getId();
                            } else {
                                return product.getSellPrice().getCurrencyProvider().getId();
                            }
                        },
                        entry -> entry.getValue().stack() * pricePerStack.get(entry.getKey()),
                        Double::sum
                ));
    }
}
