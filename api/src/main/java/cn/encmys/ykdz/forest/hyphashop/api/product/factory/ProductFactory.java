package cn.encmys.ykdz.forest.hyphashop.api.product.factory;

import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ProductFactory {
    void buildProduct(@NotNull String id, @NotNull ConfigAccessor productSection, @NotNull ConfigAccessor defaultSettings);

    @NotNull Map<String, Product> getProducts();

    @Nullable Product getProduct(@NotNull String id);

    boolean containsProduct(@NotNull String id);

    void unload();

    void save();
}
