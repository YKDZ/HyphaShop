package cn.encmys.ykdz.forest.hyphashop.api.database.schema;

import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ProductSchema(@NotNull String productId, @NotNull Map<String, Integer> currentPlayerAmount,
                            int currentGlobalAmount) {
    @Contract("_ -> new")
    public static @NotNull ProductSchema of(@NotNull ProductStock stock) {
        return new ProductSchema(stock.getProductId(), stock.getCurrentPlayerAmount(), stock.getCurrentGlobalAmount());
    }
}
