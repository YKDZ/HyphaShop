package cn.encmys.ykdz.forest.dailyshop.api.product;

import cn.encmys.ykdz.forest.dailyshop.builder.ProductIconBuilder;
import cn.encmys.ykdz.forest.dailyshop.builder.ProductItemBuilder;
import cn.encmys.ykdz.forest.dailyshop.enums.ProductType;
import cn.encmys.ykdz.forest.dailyshop.price.Price;
import cn.encmys.ykdz.forest.dailyshop.price.PricePair;
import cn.encmys.ykdz.forest.dailyshop.rarity.Rarity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class Product {
    private HashMap<String, ItemStack> productItemCache = new HashMap<>();
    private final String id;
    private final Price buyPrice;
    private final Price sellPrice;
    private final Rarity rarity;
    private final ProductIconBuilder productIconBuilder;
    private final ProductItemBuilder productItemBuilder;
    private final boolean isCacheable;

    public Product(
            String id,
            Price buyPrice,
            Price sellPrice,
            Rarity rarity,
            ProductIconBuilder productIconBuilder,
            ProductItemBuilder productItemBuilder,
            boolean isCacheable) {
        this.id = id;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.rarity = rarity;
        this.productIconBuilder = productIconBuilder;
        this.productItemBuilder = productItemBuilder;
        this.isCacheable = isCacheable;
    }

    public String getId() {
        return id;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public Price getBuyPrice() {
        return buyPrice;
    }

    public Price getSellPrice() {
        return sellPrice;
    }

    public abstract ProductType getType();

    public ProductIconBuilder getIconBuilder() {
        return productIconBuilder;
    }

    public ProductItemBuilder getProductItemBuilder() {
        return productItemBuilder;
    }

    /**
     * @param shopId Seller
     * @param player Buyer
     */
    public abstract boolean sellTo(@Nullable String shopId, Player player);

    public abstract boolean canSellTo(@Nullable String shopId, Player player);

    public abstract void give(@Nullable String shopId, @NotNull Player player);

    /**
     * @param shopId Buyer
     * @param player Seller
     */
    public abstract boolean buyFrom(@Nullable String shopId, Player player);

    /**
     * @param shopId Buyer
     * @param player Seller
     */
    public abstract int buyAllFrom(@Nullable String shopId, Player player);

    /**
     * @param shopId Buyer
     * @param player Seller
     */
    public abstract boolean canBuyFrom(@Nullable String shopId, Player player);

    public abstract boolean take(Player player, int stack);

    public abstract int takeAll(Player player);

    public ItemStack cacheProductItem(String shopId, @Nullable Player player) {
        ItemStack item = getProductItemBuilder().build(player);
        productItemCache.put(shopId, item);
        return item;
    }

    public HashMap<String, ItemStack> getProductItemCache() {
        return productItemCache;
    }

    public boolean isCacheable() {
        return isCacheable;
    }

    public boolean isCached(String shopId) {
        return getProductItemCache().containsKey(shopId);
    }

    public abstract PricePair getNewPricePair(@Nullable String shopId);
}
