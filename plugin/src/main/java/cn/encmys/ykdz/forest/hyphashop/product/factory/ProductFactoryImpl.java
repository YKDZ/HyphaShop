package cn.encmys.ykdz.forest.hyphashop.product.factory;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
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
import cn.encmys.ykdz.forest.hyphashop.config.CurrencyConfig;
import cn.encmys.ykdz.forest.hyphashop.config.ProductConfig;
import cn.encmys.ykdz.forest.hyphashop.config.RarityConfig;
import cn.encmys.ykdz.forest.hyphashop.price.PriceImpl;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import cn.encmys.ykdz.forest.hyphashop.product.ItemProduct;
import cn.encmys.ykdz.forest.hyphashop.product.VirtualProduct;
import cn.encmys.ykdz.forest.hyphashop.product.stock.ProductStockImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.ConfigUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigInheritor;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ProductFactoryImpl implements ProductFactory {
    private static final @NotNull Map<@NotNull String, @NotNull Product> products = new HashMap<>();

    public ProductFactoryImpl() {
        load();
    }

    public void load() {
        for (final String configId : ProductConfig.getAllPacksId()) {
            final ConfigAccessor config = new ConfigurationSectionAccessor(ProductConfig.getConfig(configId));

            final ConfigAccessor products = config.getConfig("products").orElse(null);
            final ConfigAccessor defaultSettings = config.getConfig("default-configs")
                    .orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));

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
    public void buildProduct(@NotNull String id, @NotNull ConfigAccessor productConfig,
                             @NotNull ConfigAccessor defaultSettings) {
        if (containsProduct(id)) {
            HyphaShopImpl.LOGGER.warn("Product id \"" + id + "\" is duplicated. Ignore this product.");
            return;
        }

        final ConfigAccessor inheritedProductConfig = new ConfigInheritor(defaultSettings, productConfig);

        final ConfigAccessor itemConfig = new ConfigInheritor(
                defaultSettings.getConfig("item").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())),
                inheritedProductConfig.getConfig("item")
                        .orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));
        final ConfigAccessor iconConfig = new ConfigInheritor(
                defaultSettings.getConfig("icon").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())),
                inheritedProductConfig.getConfig("icon")
                        .orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));

        // Icon 继承 Item
        final ConfigAccessor inheritedIconConfig = new ConfigInheritor(itemConfig, iconConfig);

        // Price
        final List<Price> buyPrice = parsePrices(defaultSettings, productConfig, "buy-price");
        final List<Price> sellPrice = parsePrices(defaultSettings, productConfig, "sell-price");

        if (buyPrice.isEmpty() && sellPrice.isEmpty()) {
            HyphaShopImpl.LOGGER.warn("""
                    Product %s has neither sell-price nor buy-price. It will be skipped.
                    """.formatted(id));
            return;
        }

        // Rarity
        final String rarityId = inheritedProductConfig.getString("rarity").orElse(RarityConfig.getAllId().getFirst());
        Rarity rarity = HyphaShop.RARITY_FACTORY.getRarity(rarityId);

        if (rarity == null) {
            HyphaShopImpl.LOGGER.warn("Product " + id + " has invalid rarity config: " + rarityId
                    + ". Use default rarity: " + RarityConfig.getAllId().getFirst());
            rarity = HyphaShop.RARITY_FACTORY.getRarity(RarityConfig.getAllId().getFirst());
        }
        assert rarity != null;

        // Cacheable
        final boolean isCacheable = inheritedProductConfig.getBoolean("cacheable").orElse(true);

        // Item (只有 ItemProduct 需要此配置)
        BaseItemDecorator itemDecorator = null;
        // 具有 item.base 配置键的商品即被视为 ItemProduct
        if (inheritedProductConfig.contains("item") && inheritedProductConfig.getConfig("item")
                .orElse(new ConfigurationSectionAccessor(new YamlConfiguration())).contains("base")) {
            var optionalDecorator = ConfigUtils.parseDecorator(itemConfig);
            if (optionalDecorator.isEmpty()) {
                HyphaShopImpl.LOGGER
                        .warn("""
                                Product %s has item.base specified but can not parse to any base item. This product will be skipped.
                                """
                                .formatted(id));
                return;
            }
            itemDecorator = optionalDecorator.get();
        }

        // 库存（可指定默认值）
        final ProductStock stock;
        final ConfigAccessor stockConfig = inheritedProductConfig.getConfig("stock")
                .orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));

        stock = new ProductStockImpl(
                id,
                stockConfig.getInt("global.size").orElse(Integer.MIN_VALUE),
                stockConfig.getInt("player.size").orElse(Integer.MIN_VALUE),
                stockConfig.getBoolean("global.replenish").orElse(false),
                stockConfig.getBoolean("player.replenish").orElse(false),
                stockConfig.getBoolean("global.overflow").orElse(false),
                stockConfig.getBoolean("player.overflow").orElse(false),
                stockConfig.getBoolean("global.inherit").orElse(false),
                stockConfig.getBoolean("player.inherit").orElse(false));

        final ProductSchema stockSchema = HyphaShop.DATABASE_FACTORY.getProductDao().querySchema(id);

        // 仅持久化 currentAmount 数据（尊重最新的 overflow, replenish, size 等配置）
        if (stockSchema != null) {
            stock.setCurrentGlobalAmount(stockSchema.currentGlobalAmount());
            stock.setCurrentPlayerAmount(stockSchema.currentPlayerAmount());
        }

        // Actions
        // 注意此处的继承关系是手动处理的
        final ConfigAccessor actionsConfig = productConfig.getConfig("actions")
                .orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));
        final ConfigAccessor defaultActionsConfig = defaultSettings.getConfig("actions")
                .orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));
        final ActionsConfig actions = ActionsConfig.of(actionsConfig);
        actions.inherit(ActionsConfig.of(defaultActionsConfig));

        // IconDecorator
        final BaseItemDecorator iconDecorator = ConfigUtils.parseDecorator(inheritedIconConfig).orElse(null);

        if (iconDecorator == null) {
            HyphaShopImpl.LOGGER
                    .warn("""
                            Can not parse to any base item from "icon.base" in product config of %s. This product will be skipped.
                            """
                            .formatted(id));
            return;
        }

        // 脚本用上下文
        final Context ctx = ScriptUtils.extractContext(inheritedProductConfig.getString("context").orElse(""));

        // 构建商品 & 储存
        if (inheritedProductConfig.contains("bundle-contents") && inheritedProductConfig.isList("bundle-contents")) {
            final Map<String, Integer> bundleContents = new HashMap<>();
            for (String contentData : inheritedProductConfig.getStringList("bundle-contents")
                    .orElse(Collections.emptyList())) {
                final String[] parsedContentData = contentData.split(":");
                if (parsedContentData.length == 1) {
                    bundleContents.put(parsedContentData[0], 1);
                } else if (parsedContentData.length == 2) {
                    bundleContents.put(parsedContentData[0], Integer.parseInt(parsedContentData[1]));
                } else {
                    HyphaShopImpl.LOGGER.warn("Product \"" + id
                            + "\" has invalid bundle-contents. The invalid line is: " + contentData + ".");
                    continue;
                }
                // 检查捆绑包内容商品是否存在
                // 需确保捆绑包商品在所有商品之后加载
                final Product content = products.get(parsedContentData[0]);
                if (content == null) {
                    HyphaShopImpl.LOGGER.warn("Bundle product \"" + id + "\" has invalid content " + contentData
                            + ". Please check and fix it in your product config.");
                    bundleContents.remove(parsedContentData[0]);
                }
            }
            products.put(id,
                    new BundleProduct(id, buyPrice, sellPrice, rarity, iconDecorator, stock, ctx, actions,
                            bundleContents));
        } else if (itemDecorator != null) {
            products.put(id,
                    new ItemProduct(id, buyPrice, sellPrice, rarity, iconDecorator, itemDecorator, stock, ctx, actions,
                            isCacheable));
        } else {
            if (actions.isEmpty())
                HyphaShopImpl.LOGGER.warn("Product \"" + id
                        + "\" has neither bundle-contents, item nor actions, which is meaningless. This product will still be loaded but will do nothing when being bought or sold. Please check your product config.");
            products.put(id,
                    new VirtualProduct(id, buyPrice, sellPrice, rarity, iconDecorator, stock, ctx, actions,
                            isCacheable));
        }
    }

    /**
     * 从默认配置中以 currency 为 id 处理价格配置的继承关系
     */
    private static @NotNull List<@NotNull Price> parsePrices(
            @NotNull ConfigAccessor defaultProductConfig,
            @NotNull ConfigAccessor productConfig,
            @NotNull String priceKey) {

        final Function<ConfigAccessor, String> currencyOf = c -> c.getString("currency").orElse(CurrencyConfig.getBaseCurrency());

        final Function<ConfigAccessor, List<? extends ConfigAccessor>> toList = c -> {
            if (c.isList(priceKey))
                return c.getConfigList(priceKey).orElse(Collections.emptyList());
            return c.getConfig(priceKey).map(Collections::singletonList).orElse(Collections.emptyList());
        };

        // 默认配置的查找表
        final Map<String, ConfigAccessor> defaultByCurrency = new HashMap<>();
        toList.apply(defaultProductConfig).forEach(c -> defaultByCurrency.put(currencyOf.apply(c), c));

        final List<Price> prices = new ArrayList<>();

        // 配置不存在，使用默认配置
        if (!productConfig.isList(priceKey) && productConfig.getConfig(priceKey).isEmpty()) {
            try {
                ConfigAccessor defaultCfg = defaultByCurrency.getOrDefault(CurrencyConfig.getBaseCurrency(),
                        new ConfigurationSectionAccessor(new YamlConfiguration()));
                prices.add(new PriceImpl(defaultCfg));
            } catch (Exception e) {
                logPriceError(e, priceKey);
                return new ArrayList<>();
            }
            return prices;
        }

        // 配置存在，正常继承
        for (ConfigAccessor cfg : toList.apply(productConfig)) {
            String cur = currencyOf.apply(cfg);
            ConfigAccessor defaultCfg = defaultByCurrency.getOrDefault(cur,
                    new ConfigurationSectionAccessor(new YamlConfiguration()));
            try {
                prices.add(new PriceImpl(new ConfigInheritor(defaultCfg, cfg)));
            } catch (Exception e) {
                logPriceError(e, priceKey);
                return new ArrayList<>();
            }
        }

        return prices;
    }

    private static void logPriceError(@NotNull Exception e, @NotNull String priceKey) {
        HyphaShopImpl.LOGGER.warn("""
                Error when parse price. %s
                %s will be disabled for security.
                """.formatted(e.getMessage(), priceKey));
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