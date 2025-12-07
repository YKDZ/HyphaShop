package cn.encmys.ykdz.forest.hyphashop.shop.stocker;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionEvent;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.product.enums.ProductType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.stocker.ShopStocker;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

public class ShopStockerImpl implements ShopStocker {
    private static final @NotNull Random RANDOM = new Random();

    private final @NotNull Shop shop;
    private final int size;
    private final @NotNull List<String> allProductsId;
    private final @NotNull List<String> listedProducts = new ArrayList<>();
    private final boolean autoRestock;
    private final long autoRestockPeriod;
    private long lastRestocking;

    public ShopStockerImpl(@NotNull Shop shop, int size, boolean autoRestock, long autoRestockPeriod, @NotNull List<String> allProductsId) {
        this.shop = shop;
        this.size = size;
        this.autoRestock = autoRestock;
        this.autoRestockPeriod = autoRestockPeriod;
        this.allProductsId = allProductsId;
    }

    @Override
    public boolean isAutoRestock() {
        return autoRestock;
    }

    @Override
    public void stock() {
        // 购物车、交易历史等菜单会用到所有商品的数量缓存
        // 故需要提前全部缓存
        allProductsId.forEach(id -> shop.getShopCounter().cacheAmount(id));

        final List<Product> productsPreparedToBeListed = new ArrayList<>();

        listedProducts.clear();
        // 映射为 productId : product
        final Map<String, Product> allProducts = allProductsId.stream()
                .map(id -> HyphaShop.PRODUCT_FACTORY.getProduct(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Product::getId, product -> product));

        if (isSizeInfinity() || size >= allProductsId.size()) {
            productsPreparedToBeListed.addAll(allProducts.values());
        } else {
            productsPreparedToBeListed.addAll(pickProductByWeight(allProducts));
        }

        // 逐个上架
        productsPreparedToBeListed.forEach(this::listProduct);

        lastRestocking = System.currentTimeMillis();

        Scheduler.runAsyncTask((task) -> shop.getShopGUI().updateContentsForAllViewers());

        MiscUtils.processActions(ActionEvent.SHOP_ON_RESTOCK, shop.getActions(), shop.getScriptContext().clone(), Map.of(), shop);
    }

    private @NotNull List<Product> pickProductByWeight(@NotNull Map<String, Product> allProducts) {
        List<Product> pickedProducts = new ArrayList<>();
        List<String> temp = new ArrayList<>(allProductsId);
        Collections.shuffle(temp); // 打乱顺序

        int productsAdded = 0;

        while (productsAdded < size && !temp.isEmpty()) {
            int totalWeight = temp.stream()
                    .map(allProducts::get)
                    .filter(Objects::nonNull)
                    .mapToInt(p -> p.getRarity().weight())
                    .sum();

            if (totalWeight <= 0) break;

            int randomValue = RANDOM.nextInt(totalWeight) + 1;
            int cumulativeWeight = 0;
            boolean foundProduct = false;
            Iterator<String> iterator = temp.iterator();

            while (iterator.hasNext()) {
                String productId = iterator.next();
                Product product = allProducts.get(productId);
                if (product == null) {
                    iterator.remove();
                    continue;
                }

                cumulativeWeight += product.getRarity().weight();
                if (cumulativeWeight < randomValue) continue;

                // 回调
                Context parent = ContextUtils.linkContext(
                        shop.getScriptContext().clone(),
                        product.getScriptContext().clone()
                );
                final List<Value> result = MiscUtils.processActionsWithResult(
                        ActionEvent.PRODUCT_ON_BEFORE_LIST, product.getActions(), parent, Map.of(), shop, product
                );
                boolean allPass = result.stream().allMatch(Value::getAsBoolean);

                if (allPass) {
                    pickedProducts.add(product);
                    productsAdded++;
                    iterator.remove();
                }

                foundProduct = true;
                break;
            }

            if (!foundProduct) break;
        }

        return pickedProducts;
    }

    @Override
    public void listProduct(@NotNull Product product) {
        final String productId = product.getId();

        // 确保每个捆绑包内容都有价格和数量缓存
        // 同时尝试缓存内容的商品物品
        if (product.getType() == ProductType.BUNDLE) {
            for (String contentId : ((BundleProduct) product).getBundleContents().keySet()) {
                HyphaShopImpl.LOGGER.debug("""
                        About to cache content %s for bundle product %s in shop %s
                        """.formatted(contentId, productId, shop.getId()));

                final Product content = HyphaShop.PRODUCT_FACTORY.getProduct(contentId);
                if (content != null) {
                    shop.getShopCounter().cacheAmount(contentId);
                    if (!shop.getShopPricer().cachePrice(contentId)) {
                        HyphaShopImpl.LOGGER.warn("Fail to list content " + contentId + " of bundle product " + productId + " cause the price is incorrect.");
                        return;
                    }
                    if (content.isProductItemCacheable() && !shop.isProductItemCached(contentId)) {
                        shop.cacheProductItem(content);
                    }
                }
            }
        }

        // 所有商品数量都提前被缓存过了

        if (!shop.getShopPricer().cachePrice(productId)) {
            HyphaShopImpl.LOGGER.warn("Fail to list product: " + productId + " cause the price is incorrect.");
            return;
        }
        if (product.isProductItemCacheable() && !shop.isProductItemCached(productId)) {
            shop.cacheProductItem(product);
        }

        // 若商品上架则补充其库存
        product.getProductStock().stock();
        // 尝试重置商人模式余额
        shop.getShopCashier().restockMerchant();

        listedProducts.add(productId);

        MiscUtils.processActions(ActionEvent.PRODUCT_ON_AFTER_LIST, product.getActions(), shop.getScriptContext(), Map.of(), product, shop);
    }

    @Override
    public boolean isSizeInfinity() {
        return size < 0;
    }

    @Override
    public long getLastRestocking() {
        return lastRestocking;
    }

    @Override
    public void setLastRestocking(long lastRestocking) {
        this.lastRestocking = lastRestocking;
    }

    @Override
    public long getAutoRestockPeriod() {
        return autoRestockPeriod;
    }

    @Override
    public @NotNull @Unmodifiable List<String> getListedProducts() {
        return Collections.unmodifiableList(listedProducts);
    }

    @Override
    public void setListedProducts(@NotNull List<String> listedProducts) {
        this.listedProducts.clear();
        this.listedProducts.addAll(listedProducts);
    }

    @Override
    public @NotNull @Unmodifiable List<String> getAllProductsId() {
        return Collections.unmodifiableList(allProductsId);
    }

    @Override
    public boolean isListedProduct(@NotNull String id) {
        return listedProducts.contains(id);
    }

    @Override
    public void addListedProducts(@NotNull List<String> listedProducts) {
        this.listedProducts.addAll(listedProducts);
    }

    @Override
    @NotNull
    public Shop getShop() {
        return shop;
    }

    @Override
    public int getSize() {
        return size;
    }
}
