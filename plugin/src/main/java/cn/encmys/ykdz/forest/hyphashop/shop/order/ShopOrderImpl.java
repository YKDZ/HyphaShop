package cn.encmys.ykdz.forest.hyphashop.shop.order;

import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.currency.CurrencyProvider;
import cn.encmys.ykdz.forest.hyphashop.api.price.PriceInstance;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.ShopCashier;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.SettlementResult;
import cn.encmys.ykdz.forest.hyphashop.price.PriceInstanceImpl;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.shop.cashier.log.SettlementLogImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class ShopOrderImpl implements ShopOrder, Cloneable {
    /**
     * 商品的交易份数
     */
    private final @NotNull Map<@NotNull ProductLocation, @NotNull Integer> orderedProducts = new HashMap<>();
    /**
     * 商品的每份价格
     */
    private final Map<ProductLocation, PriceInstance> pricePerStack = new HashMap<>();
    /**
     * 所有商品的每份价格一旦被 {@link #bill()} 固定即视为 isBilled = true，此时总价可以计算得出
     */
    private boolean isBilled = false;
    private @NotNull UUID customerUUID;
    private @NotNull OrderType type = OrderType.SELL_TO;

    public ShopOrderImpl(@NotNull UUID customerUUID) {
        this.customerUUID = customerUUID;
    }

    public ShopOrderImpl(@NotNull Player customer) {
        this(customer.getUniqueId());
    }

    @Override
    public void combineOrder(@NotNull ShopOrder newOrder) {
        if (!customerUUID.equals(newOrder.getCustomerUUID())) {
            HyphaShopImpl.LOGGER.warn("Try to combine orders with different customer.");
            return;
        }
        if (type != newOrder.getType()) {
            HyphaShopImpl.LOGGER.warn("Try to combine orders with different order types.");
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
            return new SettlementResult(SettlementResultType.INVALID_CUSTOMER, Map.of());
        }

        final SettlementResult result = new SettlementResult(canSellTo(), getTotalPrices());
        if (result.type() == SettlementResultType.SUCCESS) {
            final PriceInstance totalPrice = new PriceInstanceImpl();
            final Map<Shop, PriceInstance> shopPrices = new HashMap<>();

            // 聚合价格
            for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
                final ProductLocation productLoc = entry.getKey();
                final Shop shop = productLoc.shop().orElse(null);
                if (productLoc.product() == null || shop == null)
                    continue;

                final Map<String, Double> price = getBilledPrice(productLoc);
                totalPrice.merge(price);
                shopPrices.computeIfAbsent(shop, k -> new PriceInstanceImpl()).merge(price);
            }

            // 扣除玩家余额
            if (!totalPrice.withdraw(customer)) {
                throw new RuntimeException("""
                        Customer can not handle this price. This is a bug. Please report it.
                        """);
            }

            // 处理商家存款
            shopPrices.forEach((shop, price) -> {
                if (shop.getShopCashier().isMerchant()) {
                    shop.getShopCashier().handleDeposit(price);
                }
            });

            // 处理库存与发货
            for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
                final ProductLocation productLoc = entry.getKey();
                final Product product = productLoc.product();
                final Shop shop = productLoc.shop().orElse(null);
                final int stack = entry.getValue();

                if (product == null || shop == null)
                    continue;

                final ProductStock stock = product.getProductStock();
                if (stock.isGlobalStock())
                    stock.modifyGlobal(this);
                if (stock.isPlayerStock())
                    stock.modifyPlayer(this);

                product.give(shop, customer, stack);
            }

            log();
        }

        return result;
    }

    private @NotNull SettlementResult buyFrom() {
        final Player customer = Bukkit.getPlayer(customerUUID);
        if (customer == null)
            return new SettlementResult(SettlementResultType.INVALID_CUSTOMER, Map.of());

        final SettlementResult result = new SettlementResult(canBuyFrom(), getTotalPrices());
        if (result.type() == SettlementResultType.SUCCESS) {
            final PriceInstance totalPrice = new PriceInstanceImpl();
            final Map<Shop, PriceInstance> shopPrices = new HashMap<>();

            // 聚合价格
            for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
                final ProductLocation productLoc = entry.getKey();
                final Shop shop = productLoc.shop().orElse(null);
                if (productLoc.product() == null || shop == null)
                    continue;

                final Map<String, Double> price = getBilledPrice(productLoc);
                totalPrice.merge(price);
                shopPrices.computeIfAbsent(shop, k -> new PriceInstanceImpl()).merge(price);
            }

            // 商家扣款
            shopPrices.forEach((shop, price) -> {
                final ShopCashier cashier = shop.getShopCashier();
                if (cashier.isMerchant()) {
                    if (!cashier.canBeWithdrew(price)) {
                        throw new RuntimeException("""
                                Cashier can not handle this price. This is a bug. Please report it.
                                """);
                    }
                    cashier.handleWithdraw(price);
                }
            });

            // 玩家收款
            if (!totalPrice.deposit(customer)) {
                throw new RuntimeException("""
                        Customer can not handle this price. This is a bug. Please report it.
                        """);
            }

            // 处理库存与收货
            for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
                final ProductLocation productLoc = entry.getKey();
                final Product product = productLoc.product();
                final Shop shop = productLoc.shop().orElse(null);
                final int stack = entry.getValue();

                if (product == null || shop == null)
                    continue;

                final ProductStock stock = product.getProductStock();
                if (stock.isGlobalStock() && stock.isGlobalReplenish())
                    stock.modifyGlobal(this);
                if (stock.isPlayerStock() && stock.isGlobalReplenish())
                    stock.modifyPlayer(this);

                product.take(shop, customer, stack);
            }

            log();
        }

        return result;
    }

    /**
     * 检查是否可以将此订单中的商品出售给玩家
     *
     */
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

        // 检查单个商品的非金额条件
        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop().orElse(null);
            final int stack = entry.getValue();

            if (product == null || shop == null)
                continue;

            // 当前未上架（购物车暂存）
            if (!shop.getShopStocker().isListedProduct(product.getId())) {
                return SettlementResultType.NOT_LISTED;
            }
            // 商品未开放购买
            else if (product.getBuyPrice().isEmpty() || getBilledPrice(productLoc).isEmpty()) {
                return SettlementResultType.TRANSITION_DISABLED;
            }
            // 商品个人库存不足
            else if (product.getProductStock().isPlayerStock()
                    && product.getProductStock().isReachPlayerLimit(customerUUID, stack)) {
                return SettlementResultType.NOT_ENOUGH_PLAYER_STOCK;
            }
            // 商品总库存不足
            else if (product.getProductStock().isGlobalStock() && product.getProductStock().isReachGlobalLimit(stack)) {
                return SettlementResultType.NOT_ENOUGH_GLOBAL_STOCK;
            }
        }

        // 检查客户总余额
        final Map<String, Double> totalPrices = getTotalPrices();
        for (Map.Entry<String, Double> entry : totalPrices.entrySet()) {
            final String currencyId = entry.getKey();
            final double amount = entry.getValue();

            final CurrencyProvider currency = HyphaShop.CURRENCY_MANAGER.getCurrency(currencyId).orElse(null);

            if (currency == null || currency.getBalance(customer) < amount) {
                return SettlementResultType.NOT_ENOUGH_MONEY;
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

    /**
     * 检查是否可以从玩家手中收购订单中的商品
     *
     */
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

        // 用于聚合每个商店的总价
        final Map<Shop, Map<String, Double>> shopTotalPrices = new HashMap<>();

        // 检查单个商品的非金额条件
        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop().orElse(null);
            final int stack = entry.getValue();

            if (product == null || shop == null)
                continue;

            // 聚合价格
            final Map<String, Double> itemPrice = getBilledPrice(productLoc);
            final Map<String, Double> shopMap = shopTotalPrices.computeIfAbsent(shop, k -> new HashMap<>());
            itemPrice.forEach((currency, amount) -> shopMap.merge(currency, amount, Double::sum));

            // 当前未上架（购物车暂存）
            if (!shop.getShopStocker().isListedProduct(product.getId())) {
                return SettlementResultType.NOT_LISTED;
            }
            // 商品未开放收购
            else if (product.getSellPrice().isEmpty() || getBilledPrice(productLoc).isEmpty()) {
                return SettlementResultType.TRANSITION_DISABLED;
            }
            // 客户没有足够的商品
            else if (product.has(shop, customer, stack) == 0) {
                return SettlementResultType.NOT_ENOUGH_PRODUCT;
            }
        }

        // 检查每个商店的余额 (商人模式)
        for (Map.Entry<Shop, Map<String, Double>> entry : shopTotalPrices.entrySet()) {
            final Shop shop = entry.getKey();
            final Map<String, Double> prices = entry.getValue();
            final ShopCashier cashier = shop.getShopCashier();

            if (cashier.isMerchant() && !cashier.canBeWithdrew(new PriceInstanceImpl(prices))) {
                return SettlementResultType.NOT_ENOUGH_MERCHANT_BALANCE;
            }
        }
        return SettlementResultType.SUCCESS;
    }

    @Override
    public boolean canHold() {
        final Player customer = Bukkit.getPlayer(customerUUID);
        if (customer == null)
            return false;

        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop().orElse(null);
            final int stack = entry.getValue();

            if (product == null || shop == null)
                continue;

            if (!product.canHold(shop, customer, stack))
                return false;
        }

        return true;
    }

    /**
     * 查找并固定每个待交易商品的价格
     */
    @Override
    public void bill() {
        if (isBilled())
            return;

        final Map<ProductLocation, PriceInstance> prices = new HashMap<>();
        for (final Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final String productId = productLoc.productId();
            final Shop shop = productLoc.shop().orElse(null);

            if (shop == null)
                continue;

            final PriceInstance price = type == OrderType.SELL_TO ? shop.getShopPricer().getBuyPrice(productId)
                    : shop.getShopPricer().getSellPrice(productId);

            // 空价格没有记录的意义
            if (price.isEmpty())
                continue;

            prices.put(productLoc, price);
        }

        setPrices(prices);
        setBilled(true);
    }

    private void log() {
        final SettlementLog log = new SettlementLogImpl(customerUUID, type);

        Map<ProductLocation, AmountPair> orderResult = new HashMap<>();
        for (Map.Entry<ProductLocation, Integer> entry : orderedProducts.entrySet()) {
            final ProductLocation productLoc = entry.getKey();
            final Product product = productLoc.product();
            final Shop shop = productLoc.shop().orElse(null);
            final int stack = entry.getValue();

            if (product == null || shop == null)
                continue;

            final int amount = shop.getShopCounter().getAmount(productLoc.productId());

            orderResult.put(productLoc, new AmountPair(amount, stack));
        }

        Scheduler.runAsyncTask((task) -> HyphaShop.DATABASE_FACTORY.getSettlementLogDao().insertLog(log
                .setOrderedProducts(orderResult)
                .setPricePerStack(pricePerStack)));
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

    /**
     * 获取商品被 bill 时固定下的总价格
     */
    @Override
    public @NotNull Map<@NotNull String, @NotNull Double> getBilledPrice(@NotNull ProductLocation productLoc) {
        if (!isBilled)
            bill();

        final PriceInstance perStack = pricePerStack.get(productLoc);
        final Integer stack = orderedProducts.get(productLoc);

        if (perStack == null || stack == null) {
            HyphaShopImpl.LOGGER.warn("""
                    Try to get billed price for product id %s in shop %s, which does exists in this order. This is likely a bug.
                    """.formatted(productLoc.productId(), productLoc.shopId()));
            HyphaShopImpl.LOGGER.debug("""
                    Related order: %s
                    """.formatted(this));
            return Collections.emptyMap();
        }

        return perStack.mul(stack);
    }

    @Override
    public @NotNull ShopOrder setPrices(@NotNull @Unmodifiable Map<ProductLocation, PriceInstance> prices) {
        this.pricePerStack.clear();
        this.pricePerStack.putAll(prices);
        setBilled(false);
        return this;
    }

    /**
     * 获取本订单指定货币类型的总金额
     */
    @Override
    public double getTotalPrice(@NotNull String currencyId) {
        if (!isBilled())
            bill();
        return getTotalPrices().getOrDefault(currencyId, Double.NaN);
    }

    /**
     * 获取本订单所有货币类型的总金额
     */
    @Override
    public @NotNull Map<String, @NotNull Double> getTotalPrices() {
        if (!isBilled())
            bill();

        final Map<@NotNull String, @NotNull Double> result = new HashMap<>();

        for (Map.Entry<ProductLocation, Integer> orderedProduct : orderedProducts.entrySet()) {
            final PriceInstance priceInstance = pricePerStack.get(orderedProduct.getKey());
            if (priceInstance == null)
                continue;

            priceInstance.getPrices().forEach((currencyId, price) -> result.merge(
                    currencyId,
                    price,
                    Double::sum));
        }

        return result;
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
            final Shop shop = productLoc.shop().orElse(null);
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
            // 不允许购买
            else if (product.getBuyPrice().isEmpty() && type == OrderType.SELL_TO) {
                iterator.remove();
            }
            // 不允许收购
            else if (product.getSellPrice().isEmpty() && type != OrderType.SELL_TO) {
                iterator.remove();
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
                    .setPrices(pricePerStack)
                    .setBilled(isBilled)
                    .setOrderedProducts(orderedProducts);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "ShopOrderImpl{" +
                "orderedProducts=" + orderedProducts +
                ", pricePerStack=" + pricePerStack +
                ", isBilled=" + isBilled +
                ", customerUUID=" + customerUUID +
                ", type=" + type +
                '}';
    }
}
