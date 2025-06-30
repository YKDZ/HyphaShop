package cn.encmys.ykdz.forest.hyphashop.product.factory;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProductDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ProductSchema;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.product.factory.ProductFactory;
import cn.encmys.ykdz.forest.hyphashop.api.product.stock.ProductStock;
import cn.encmys.ykdz.forest.hyphashop.api.rarity.Rarity;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.config.ProductConfig;
import cn.encmys.ykdz.forest.hyphashop.config.RarityConfig;
import cn.encmys.ykdz.forest.hyphashop.price.PriceImpl;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import cn.encmys.ykdz.forest.hyphashop.product.ItemProduct;
import cn.encmys.ykdz.forest.hyphashop.product.VirtualProduct;
import cn.encmys.ykdz.forest.hyphashop.product.stock.ProductStockImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.ConfigUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigInheritor;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProductFactoryImpl implements ProductFactory {
    private static final @NotNull Map<@NotNull String, @NotNull Product> products = new HashMap<>();

    public ProductFactoryImpl() {
        load();
    }

    public void load() {
        for (final String configId : ProductConfig.getAllPacksId()) {
            final ConfigAccessor config = new ConfigurationSectionAccessor(ProductConfig.getConfig(configId));

            final ConfigAccessor products = config.getConfig("products").orElse(null);
            final ConfigAccessor defaultSettings = config.getConfig("default-settings").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));

            if (products == null) {
                continue;
            }

            final List<String> bundleProducts = new ArrayList<>();
            for (final String productId : products.getKeys()) {
                final ConfigAccessor productConfig = products.getConfig(productId).orElse(null);
                // 最后再构建捆绑包
                if (productConfig != null && productConfig.contains("bundle-contents")) {
                    bundleProducts.add(productId);
                } else if (productConfig != null) {
                    buildProduct(productId, productConfig, defaultSettings);
                } else {
                    throw new IllegalArgumentException("Product " + productId + " has no config section.");
                }
            }

            // 最后构建捆绑包商品以便进行内容可用性检查
            for (final String bundleProductId : bundleProducts) {
                final ConfigAccessor productSection = products.getConfig(bundleProductId).orElse(null);
                if (productSection == null)
                    throw new IllegalArgumentException("Bundle product " + bundleProductId + " has no config section.");
                buildProduct(bundleProductId, productSection, defaultSettings);
            }
        }
    }

    @Override
    public void buildProduct(@NotNull String id, @NotNull ConfigAccessor productConfig, @NotNull ConfigAccessor defaultSettings) {
        if (containsProduct(id)) {
            LogUtils.warn("Product id \"" + id + "\" is duplicated. Ignore this product.");
            return;
        }

        final ConfigAccessor inheritedProductConfig = new ConfigInheritor(defaultSettings, productConfig);

        final ConfigAccessor itemConfig = new ConfigInheritor(defaultSettings.getConfig("item").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())), inheritedProductConfig.getConfig("item").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));
        final ConfigAccessor iconConfig = new ConfigInheritor(defaultSettings.getConfig("icon").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())), inheritedProductConfig.getConfig("icon").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));

        // Icon 继承 Item
        final ConfigAccessor inheritedIconConfig = new ConfigInheritor(itemConfig, iconConfig);

        // Price
        final Price buyPrice = new PriceImpl(
                defaultSettings.getConfig("buy-price").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())),
                inheritedProductConfig.getConfig("buy-price").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()))
        );
        final Price sellPrice = new PriceImpl(
                defaultSettings.getConfig("sell-price").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())),
                inheritedProductConfig.getConfig("sell-price").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()))
        );

        // Rarity
        final String rarityId = inheritedProductConfig.getString("rarity").orElse(RarityConfig.getAllId().getFirst());
        Rarity rarity = HyphaShop.RARITY_FACTORY.getRarity(rarityId);

        if (rarity == null) {
            LogUtils.warn("Product " + id + " has invalid rarity config: " + rarityId + ". Use default rarity: " + RarityConfig.getAllId().getFirst());
            rarity = HyphaShop.RARITY_FACTORY.getRarity(RarityConfig.getAllId().getFirst());
        }
        assert rarity != null;

        // Cacheable
        final boolean isCacheable = inheritedProductConfig.getBoolean("cacheable").orElse(true);

        // Item (只有 ItemProduct 需要此配置)
        BaseItemDecorator itemDecorator = null;
        // 具有 item.base 配置键的商品即被视为 ItemProduct
        if (inheritedProductConfig.contains("item") && inheritedProductConfig.getConfig("item").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())).contains("base")) {
            itemDecorator = ConfigUtils.parseDecorator(itemConfig);
        }

        // 库存（可指定默认值）
        final ProductStock stock;
        final ConfigAccessor stockConfig = inheritedProductConfig.getConfig("stock").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));

        stock = new ProductStockImpl(
                id,
                stockConfig.getInt("global.size").orElse(Integer.MIN_VALUE),
                stockConfig.getInt("player.size").orElse(Integer.MIN_VALUE),
                stockConfig.getBoolean("global.replenish").orElse(false),
                stockConfig.getBoolean("player.replenish").orElse(false),
                stockConfig.getBoolean("global.overflow").orElse(false),
                stockConfig.getBoolean("player.overflow").orElse(false),
                stockConfig.getBoolean("global.inherit").orElse(false),
                stockConfig.getBoolean("player.inherit").orElse(false)
        );

        final ProductSchema stockSchema = HyphaShop.DATABASE_FACTORY.getProductDao().querySchema(id);

        // 仅持久化 currentAmount 数据（尊重最新的 overflow, replenish, size 等配置）
        if (stockSchema != null) {
            stock.setCurrentGlobalAmount(stockSchema.currentGlobalAmount());
            stock.setCurrentPlayerAmount(stockSchema.currentPlayerAmount());
        }

        // Actions
        // 注意此处的继承关系是手动处理的
        final ConfigAccessor actionsConfig = productConfig.getConfig("actions").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));
        final ConfigAccessor defaultActionsConfig = defaultSettings.getConfig("actions").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));
        final ActionsConfig actions = ActionsConfig.of(actionsConfig);
        actions.inherit(ActionsConfig.of(defaultActionsConfig));

        // IconDecorator
        final BaseItemDecorator iconDecorator = ConfigUtils.parseDecorator(inheritedIconConfig);

        // 脚本用上下文
        final Context ctx = ScriptUtils.extractContext(inheritedProductConfig.getString("context").orElse(""));

        // 构建商品 & 储存
        if (inheritedProductConfig.contains("bundle-contents") && inheritedProductConfig.isList("bundle-contents")) {
            final Map<String, Integer> bundleContents = new HashMap<>();
            for (String contentData : inheritedProductConfig.getStringList("bundle-contents").orElse(Collections.emptyList())) {
                final String[] parsedContentData = contentData.split(":");
                if (parsedContentData.length == 1) {
                    bundleContents.put(parsedContentData[0], 1);
                } else if (parsedContentData.length == 2) {
                    bundleContents.put(parsedContentData[0], Integer.parseInt(parsedContentData[1]));
                } else {
                    LogUtils.warn("Product \"" + id + "\" has invalid bundle-contents. The invalid line is: " + contentData + ".");
                    continue;
                }
                // 检查捆绑包内容商品是否存在
                // 需确保捆绑包商品在所有商品之后加载
                final Product content = products.get(parsedContentData[0]);
                if (content == null) {
                    LogUtils.warn("Bundle product \"" + id + "\" has invalid content " + contentData + ". Please check and fix it in your product config.");
                    bundleContents.remove(parsedContentData[0]);
                }
            }
            products.put(id,
                    new BundleProduct(id, buyPrice, sellPrice, rarity, iconDecorator, stock, ctx, actions, bundleContents));
        } else if (itemDecorator != null) {
            products.put(id,
                    new ItemProduct(id, buyPrice, sellPrice, rarity, iconDecorator, itemDecorator, stock, ctx, actions, isCacheable));
        } else {
            if (actions.isEmpty())
                LogUtils.warn("Product \"" + id + "\" has neither bundle-contents, item nor actions, which is meaningless. This product will still be loaded but will do nothing when being bought or sold. Please check your product config.");
            products.put(id,
                    new VirtualProduct(id, buyPrice, sellPrice, rarity, iconDecorator, stock, ctx, actions, isCacheable));
        }
    }

    @Override
    public @NotNull Map<String, Product> getProducts() {
        return Collections.unmodifiableMap(products);
    }

    @Override
    public @Nullable Product getProduct(@NotNull String id) {
        return products.get(id);
    }

    @Override
    public boolean containsProduct(@NotNull String id) {
        return products.containsKey(id);
    }

    @Override
    public void unload() {
        save();
        products.clear();
    }

    @Override
    public void save() {
        products.values().forEach(product -> {
            final ProductDao dao = HyphaShop.DATABASE_FACTORY.getProductDao();
            dao.insertSchema(ProductSchema.of(product.getProductStock()));
        });
    }
}