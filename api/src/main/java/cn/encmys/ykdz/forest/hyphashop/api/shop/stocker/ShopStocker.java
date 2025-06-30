package cn.encmys.ykdz.forest.hyphashop.api.shop.stocker;

import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ShopStocker {
    boolean isAutoRestock();

    void stock();

    void listProduct(Product product);

    boolean isSizeInfinity();

    long getLastRestocking();

    void setLastRestocking(long lastRestocking);

    /**
     * @return Period in tick
     */
    long getAutoRestockPeriod();

    List<String> getListedProducts();

    void setListedProducts(@NotNull List<String> listedProducts);

    List<String> getAllProductsId();

    boolean isListedProduct(String id);

    void addListedProducts(List<String> listedProducts);

    int getSize();

    Shop getShop();
}
