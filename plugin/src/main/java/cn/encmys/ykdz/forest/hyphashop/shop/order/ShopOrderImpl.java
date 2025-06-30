package cn.encmys.ykdz.forest.hyphashop.shop.order;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceMode;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.SettlementResult;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.shop.cashier.log.SettlementLogImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.BalanceUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class ShopOrderImpl implements ShopOrder, Cloneable {
    private final @NotNull Map<@NotNull ProductLocation, @NotNull Integer> orderedProducts = new HashMap<>();
    private final Map<ProductLocation, Double> bill = new HashMap<>();
    private @NotNull UUID customerUUID;
    private @NotNull OrderType type = OrderType.SELL_TO;
    private boolean isBilled = false;

    public ShopOrderImpl(@NotNull UUID customerUUID) {
        this.customerUUID = customerUUID;
    }

    public ShopOrderImpl(@NotNull Player customer) {
        this(customer.getUniqueId());
    }

    @Override
    public void combineOrder(@NotNull ShopOrder newOrder) {
        if (!customerUUID.equals(newOrder.getCustomerUUID())) {
            LogUtils.warn("Try to combine orders with different customer.");
            return;
        }
        if (type != newOrder.getType()) {
            LogUtils.warn("Try to combine orders with different order types.");
            return;
        }
        // 新定单合并到本定单
        newOrder.getOrderedProducts()
                .forEach(this::modifyStack);
        setBilled(false);
    }

    @Override
    public @NotNull SettlementResult settle() {
        bill();
        return switch (type) {
            case BUY_FROM, BUY_ALL_FROM -> buyFrom();
            case SELL_TO -> sellTo();
        };
    }

    private @NotNull SettlementResult sellTo() {
        final Player customer = Bukkit.getPlayer(customerUUID);
        if (customer == null) {
            return new SettlementResult(SettlementResultType.INVALID_CUSTOMER, 0d);
        }

        final SettlementResult result = new SettlementResult(canSellTo(), getTotalPrice());
        if (result.type() == SettlementResultType.SUCCESS) {
            for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
                ProductLocation productLoc = entry.getKey();
                Product product = productLoc.product();
                Shop shop = productLoc.shop();
                int stack = entry.getValue();

                if (product == null || shop == null) continue;

                // 处理商人模式
                if (shop.getShopCashier().isMerchant() && (shop.getShopCashier().isReplenish() && getTotalPrice() > 0))
                    shop.getShopCashier().modifyBalance(getTotalPrice());

                // 处理库存
                ProductStock stock = product.getProductStock();
                if (stock.isGlobalStock()) stock.modifyGlobal(this);
                if (stock.isPlayerStock()) stock.modifyPlayer(this);

                // 处理余额
                BalanceUtils.removeBalance(customer, getBilledPrice(productLoc));

                // 给予商品
                product.give(shop, customer, stack);
            }

            log();
        }

        return result;
    }

    private @NotNull SettlementResult buyFrom() {
        final Player customer = Bukkit.getPlayer(customerUUID);
        if (customer == null) return new SettlementResult(SettlementResultType.INVALID_CUSTOMER, 0d);

        final SettlementResult result = new SettlementResult(canBuyFrom(), getTotalPrice());
        if (result.type() == SettlementResultType.SUCCESS) {
            for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
                ProductLocation productLoc = entry.getKey();
                Product product = productLoc.product();
                Shop shop = productLoc.shop();
                int stack = entry.getValue();

                if (product == null || shop == null) continue;

                // 处理商人模式
                if (shop.getShopCashier().isMerchant())
                    shop.getShopCashier().modifyBalance(-1 * getTotalPrice());

                // 处理库存
                ProductStock stock = product.getProductStock();
                if (stock.isGlobalStock() && stock.isGlobalReplenish()) stock.modifyGlobal(this);
                if (stock.isPlayerStock() && stock.isGlobalReplenish()) stock.modifyPlayer(this);

                // 处理余额
                BalanceUtils.addBalance(customer, getBilledPrice(productLoc));

                // 收取商品
                product.take(shop, customer, stack);
            }

            log();
        }

        return result;
    }

    @Override
    public @NotNull SettlementResultType canSellTo() {
        final Player customer = Bukkit.getPlayer(customerUUID);

        // 顾客不存在
        if (customer == null) {
            return SettlementResultType.INVALID_CUSTOMER;
        }
        // 订单为空（实际上在 ProfileImpl#settleCart 中用于处理购物车为空的情况）
        else if (orderedProducts.isEmpty()) {
            return SettlementResultType.EMPTY;
        }

        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop();
            final int stack = entry.getValue();

            if (product == null || shop == null) continue;

            // 当前未上架（购物车暂存）
            if (!shop.getShopStocker().isListedProduct(product.getId())) {
                return SettlementResultType.NOT_LISTED;
            }
            // 商品未开放购买
            else if (product.getBuyPrice().getPriceMode() == PriceMode.DISABLE || Double.isNaN(getBilledPrice(productLoc))) {
                return SettlementResultType.TRANSITION_DISABLED;
            }
            // 客户余额不足
            else if (BalanceUtils.checkBalance(customer) < getBilledPrice(productLoc)) {
                return SettlementResultType.NOT_ENOUGH_MONEY;
            }
            // 商品个人库存不足
            else if (product.getProductStock().isPlayerStock() && product.getProductStock().isReachPlayerLimit(customerUUID, stack)) {
                return SettlementResultType.NOT_ENOUGH_PLAYER_STOCK;
            }
            // 商品总库存不足
            else if (product.getProductStock().isGlobalStock() && product.getProductStock().isReachGlobalLimit(stack)) {
                return SettlementResultType.NOT_ENOUGH_GLOBAL_STOCK;
            }
        }

        // 客户背包空间不足
        // 需要保证所有商品都上架才能检查
        // 故放在最后
        if (!canHold()) {
            return SettlementResultType.NOT_ENOUGH_INVENTORY_SPACE;
        }

        return SettlementResultType.SUCCESS;
    }

    @Override
    public @NotNull SettlementResultType canBuyFrom() {
        final Player customer = Bukkit.getPlayer(customerUUID);

        // 顾客不存在
        if (customer == null) {
            return SettlementResultType.INVALID_CUSTOMER;
        }
        // 订单为空
        else if (orderedProducts.isEmpty()) {
            return SettlementResultType.EMPTY;
        }

        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop();
            final int stack = entry.getValue();

            if (product == null || shop == null) continue;

            // 当前未上架（购物车暂存）
            if (!shop.getShopStocker().isListedProduct(product.getId())) {
                return SettlementResultType.NOT_LISTED;
            }
            // 商品未开放收购
            else if (product.getSellPrice().getPriceMode() == PriceMode.DISABLE || Double.isNaN(getBilledPrice(productLoc))) {
                return SettlementResultType.TRANSITION_DISABLED;
            }
            // 商人模式余额不足
            else if (shop.getShopCashier().isMerchant() && shop.getShopCashier().getBalance() < getTotalPrice()) {
                return SettlementResultType.NOT_ENOUGH_MERCHANT_BALANCE;
            }
            // 客户没有足够的商品
            else if (product.has(shop, customer, stack) == 0) {
                return SettlementResultType.NOT_ENOUGH_PRODUCT;
            }
        }
        return SettlementResultType.SUCCESS;
    }

    @Override
    public boolean canHold() {
        final Player customer = Bukkit.getPlayer(customerUUID);
        if (customer == null) return false;


        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop();
            final int stack = entry.getValue();

            if (product == null || shop == null) continue;

            if (!product.canHold(shop, customer, stack)) return false;
        }

        return true;
    }

    @Override
    public void bill() {
        if (isBilled()) return;

        final Map<ProductLocation, Double> bill = new HashMap<>();
        for (final Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final String productId = productLoc.productId();
            final Shop shop = productLoc.shop();
            final int stack = entry.getValue();
            final double price;

            if (shop == null) continue;

            if (type == OrderType.SELL_TO) {
                price = shop.getShopPricer().getBuyPrice(productId) * stack;
            } else {
                price = shop.getShopPricer().getSellPrice(productId) * stack;
            }

            bill.put(productLoc, price);
        }

        setBill(bill);
        setBilled(true);
    }

    private void log() {
        final SettlementLog log = new SettlementLogImpl(customerUUID, type);

        Map<ProductLocation, AmountPair> orderResult = new HashMap<>();
        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop();
            final int stack = entry.getValue();

            if (product == null || shop == null) continue;

            final int amount = shop.getShopCounter().getAmount(productLoc.productId());

            orderResult.put(productLoc, new AmountPair(amount, stack));
        }

        Scheduler.runAsyncTask((task) -> HyphaShop.DATABASE_FACTORY.getSettlementLogDao().insertLog(log
                .setOrderedProducts(orderResult)
                .setBill(bill)
        ));
    }

    @Override
    public @NotNull ShopOrder setType(@NotNull OrderType orderType) {
        this.type = orderType;
        this.isBilled = false;
        return this;
    }

    @Override
    public @NotNull ShopOrder modifyStack(@NotNull ProductLocation productLoc, int amount) {
        final int newValue = orderedProducts.getOrDefault(productLoc, 0) + amount;
        return setStack(productLoc, newValue);
    }

    @Override
    public @NotNull ShopOrder setStack(@NotNull ProductLocation productLoc, int amount) {
        if (amount <= 0) {
            orderedProducts.remove(productLoc);
        } else {
            orderedProducts.put(productLoc, amount);
        }
        setBilled(false);
        return this;
    }

    @Override
    public @NotNull OrderType getType() {
        return type;
    }

    @Override
    public @NotNull UUID getCustomerUUID() {
        return customerUUID;
    }

    @Override
    public @NotNull @Unmodifiable Map<ProductLocation, Integer> getOrderedProducts() {
        return Collections.unmodifiableMap(orderedProducts);
    }

    @Override
    public double getBilledPrice(@NotNull ProductLocation productLoc) {
        if (!isBilled()) bill();
        return bill.getOrDefault(productLoc, Double.NaN);
    }

    @Override
    public @NotNull ShopOrder setBill(@NotNull @Unmodifiable Map<ProductLocation, Double> bill) {
        this.bill.clear();
        this.bill.putAll(bill);
        setBilled(false);
        return this;
    }

    @Override
    public double getTotalPrice() {
        if (!isBilled()) bill();
        return bill.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    @Override
    public boolean isBilled() {
        return isBilled;
    }

    @Override
    public @NotNull ShopOrder setBilled(boolean billed) {
        isBilled = billed;
        return this;
    }

    @Override
    public void clear() {
        isBilled = false;
        orderedProducts.clear();
    }

    @Override
    public void clean() {
        final Iterator<Map.Entry<ProductLocation, Integer>> iterator = orderedProducts.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<ProductLocation, Integer> entry = iterator.next();

            final ProductLocation productLoc = entry.getKey();
            final String productId = productLoc.productId();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop();
            final int stack = entry.getValue();

            // 商品 / 商店不存在
            if (product == null || shop == null) {
                iterator.remove();
            }
            // 商品未上架
            else if (!shop.getShopStocker().isListedProduct(productId)) {
                iterator.remove();
            }
            // 商品库存不足
            else if (product.getProductStock().isStock()) {
                final ProductStock stock = product.getProductStock();
                // 公共库存
                if (stock.isGlobalStock() && stock.getCurrentGlobalAmount() < stack) {
                    iterator.remove();
                }
                // 玩家库存
                else if (stock.isPlayerStock() && stock.getCurrentPlayerAmount(customerUUID) < stack) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public @NotNull ShopOrder setOrderedProducts(@NotNull Map<ProductLocation, Integer> orderedProducts) {
        this.orderedProducts.clear();
        this.orderedProducts.putAll(orderedProducts);
        return this;
    }

    @Override
    public @NotNull ShopOrder setCustomerUUID(@NotNull UUID customerUUID) {
        this.customerUUID = customerUUID;
        return this;
    }

    @Override
    public @NotNull ShopOrder clone() {
        try {
            return ((ShopOrder) super.clone())
                    .setCustomerUUID(customerUUID)
                    .setType(type)
                    .setBill(bill)
                    .setBilled(isBilled)
                    .setOrderedProducts(orderedProducts);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
