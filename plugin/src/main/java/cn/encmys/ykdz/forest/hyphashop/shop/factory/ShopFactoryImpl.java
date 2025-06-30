package cn.encmys.ykdz.forest.hyphashop.shop.factory;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ShopSchema;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.factory.ShopFactory;
import cn.encmys.ykdz.forest.hyphashop.config.ProductConfig;
import cn.encmys.ykdz.forest.hyphashop.config.ShopConfig;
import cn.encmys.ykdz.forest.hyphashop.shop.ShopImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class ShopFactoryImpl implements ShopFactory {
    @NotNull
    private static final HashMap<String, Shop> shops = new HashMap<>();

    public ShopFactoryImpl() {
        load();
    }

    @Override
    public void load() {
        for (String id : ShopConfig.getAllId()) {
            buildShop(id);
        }
    }

    @Override
    public @NotNull Shop buildShop(@NotNull String id) {
        if (shops.containsKey(id)) {
            throw new IllegalArgumentException("Shop ID is duplicated: " + id);
        }

        List<String> products = new ArrayList<>();

        for (String productId : ShopConfig.getAllProductsId(id)) {
            // 处理 PACK:XXX 的包导入格式
            if (productId.startsWith("PACK:")) {
                String packId = productId.substring(5);
                List<String> packProducts = ProductConfig.getAllProductId(packId);
                if (packProducts == null) {
                    LogUtils.warn("Product pack " + packId + ".yml in shop " + id + " not found.");
                    continue;
                }
                products.addAll(packProducts);
                continue;
            }

            products.add(productId);
        }

        // 检查商店导入的商品是否存在
        products = products.stream()
                .filter(productId -> {
                    if (!HyphaShop.PRODUCT_FACTORY.containsProduct(productId)) {
                        LogUtils.warn("Product " + productId + " in shop " + id + " not exist.");
                        return false;
                    }
                    return true;
                })
                .toList();

        Shop shop = new ShopImpl(
                id,
                ShopConfig.getShopSettingsRecord(id),
                products
        );

        // 从数据库加载一系列商店数据
        ShopSchema schema = HyphaShop.DATABASE_FACTORY.getShopDao().querySchema(shop.getId());
        if (schema != null) {
            if (shop.getShopCashier().isInherit()) shop.getShopCashier().setBalance(schema.balance());
            shop.getShopCounter().setCachedAmounts(schema.cachedAmounts());
            shop.getShopPricer().setCachedPrices(schema.cachedPrices());
            shop.getShopStocker().setListedProducts(schema.listedProducts());
            shop.getShopStocker().setLastRestocking(schema.lastRestocking());
        } else {
            // 为新增商店填充初始商品
            shop.getShopStocker().stock();
        }
        // 加载完成

        shops.put(id, shop);
        LogUtils.info("Successfully load shop " + id + " with " + products.size() + " products.");
        return shop;
    }

    @Override
    public @NotNull Shop getShop(@NotNull String id) {
        return shops.get(id);
    }

    @Override
    public @NotNull @Unmodifiable Map<String, Shop> getShops() {
        return Collections.unmodifiableMap(shops);
    }

    @Override
    public void unload() {
        save();
        shops.clear();
    }

    @Override
    public void save() {
        shops.values().forEach(shop -> HyphaShop.DATABASE_FACTORY.getShopDao().insertSchema(ShopSchema.of(shop)));
    }
}
