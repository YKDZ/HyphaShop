package cn.encmys.ykdz.forest.hyphashop.api.shop.order.record;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

public record ProductLocation(@NotNull @Expose String shopId, @NotNull @Expose String productId) {
    public @NotNull Optional<Shop> shop() {
        return HyphaShop.SHOP_FACTORY.getShop(shopId);
    }

    public @Nullable Product product() {
        return HyphaShop.PRODUCT_FACTORY.getProduct(productId);
    }

    public static class Adapter extends TypeAdapter<ProductLocation> {
        @Override
        public void write(JsonWriter out, ProductLocation location) throws IOException {
            if (location == null) {
                out.nullValue();
                return;
            }
            // id:productId
            out.value(location.shopId + ":" + location.productId());
        }

        @Override
        public ProductLocation read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String raw = in.nextString();
            String[] parts = raw.split(":");
            if (parts.length != 2) {
                throw new IOException("Invalid ProductLocation format: " + raw);
            }
            return new ProductLocation(parts[0], parts[1]);
        }
    }
}
