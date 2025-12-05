package cn.encmys.ykdz.forest.hyphashop.shop.pricer;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.price.PriceInstance;
import cn.encmys.ykdz.forest.hyphashop.api.price.PricePair;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceMode;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.pricer.ShopPricer;
import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphashop.price.PriceInstanceImpl;
import cn.encmys.ykdz.forest.hyphashop.price.PricePairImpl;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import cn.encmys.ykdz.forest.hyphashop.shop.ShopImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ShopPricerImpl implements ShopPricer {
    private @NotNull Map<String, PricePair> cachedPrices = new HashMap<>();
    private final @NotNull Shop shop;

    public ShopPricerImpl(@NotNull ShopImpl shop) {
        this.shop = shop;
    }

    @Override
    public @NotNull PriceInstance getBuyPrice(@NotNull String productId) {
        return getPrice(productId, PricePair::getBuy, "buy");
    }

    @Override
    public @NotNull PriceInstance getSellPrice(@NotNull String productId) {
        return getPrice(productId, PricePair::getSell, "sell");
    }

    private @NotNull PriceInstance getPrice(@NotNull String productId,
                                            @NotNull Function<PricePair, PriceInstance> extractor, @NotNull String priceType) {
        final PricePair pricePair = cachedPrices.get(productId);
        if (pricePair == null)
            throw new RuntimeException("""
                    Product %s do not have %s price cached. This is a bug. Please report it.
                    """.formatted(productId, priceType));

        return extractor.apply(pricePair);
    }

    @Override
    public boolean cachePrice(@NotNull String productId) {
        final Product product = HyphaShop.PRODUCT_FACTORY.getProduct(productId);
        if (product == null) {
            HyphaShopImpl.LOGGER.warn("Try to cache price for product " + productId + " which does not exist.");
            return false;
        }

        final Map<String, Double> buyPrice = calculatePrice(product.getBuyPrice(), product, true);
        final Map<String, Double> sellPrice = calculatePrice(product.getSellPrice(), product, false);

        if (!checkPrice(buyPrice, sellPrice)) {
            HyphaShopImpl.LOGGER.warn("""
                    Buy price (%s) <= sell price (%s) for %s. Disabling %s.
                    """.formatted(buyPrice, sellPrice, productId,
                    Config.priceCorrectByDisableSellOrBuy ? "sell" : "buy"));

            if (Config.priceCorrectByDisableSellOrBuy)
                sellPrice.clear();
            else
                buyPrice.clear();
        }

        if (buyPrice.isEmpty() && sellPrice.isEmpty()) {
            HyphaShopImpl.LOGGER.warn("""
                    Price cache for %s failed cause both buy (%s) and sell (%s) are invalid.
                    """.formatted(productId, buyPrice, sellPrice));
            return false;
        }

        cachedPrices.put(productId, getModifiedPricePair(productId,
                new PricePairImpl(new PriceInstanceImpl(buyPrice), new PriceInstanceImpl(sellPrice))));
        return true;
    }

    /**
     * 仅当购买价和收购价全部存在、且收购价的货币种类是出售价的子集、且对应的值全部大于收购价格时返回 false<br/>
     * 用于尽力避免高买低卖问题，因为货币间的汇率是不可知的
     *
     * @param buyPrice  出售价
     * @param sellPrice 收购价
     * @return 此价格对是否合法
     */
    private boolean checkPrice(
            @NotNull Map<String, Double> buyPrice,
            @NotNull Map<String, Double> sellPrice) {
        if (buyPrice.isEmpty() || sellPrice.isEmpty()) {
            return true;
        }

        for (String currencyId : sellPrice.keySet()) {
            if (!buyPrice.containsKey(currencyId)) {
                return true;
            }
        }

        for (Map.Entry<String, Double> entry : sellPrice.entrySet()) {
            String currencyId = entry.getKey();
            Double sell = entry.getValue();
            Double buy = buyPrice.get(currencyId);

            if (sell < buy) {
                return true;
            }
        }

        return false;
    }

    private @NotNull Map<@NotNull String, @NotNull Double> calculatePrice(@NotNull List<Price> prices,
                                                                          @NotNull Product product, boolean isBuy) {
        final Map<String, Double> result = new HashMap<>();

        for (Price price : prices) {
            final double priceValue = price.getPriceMode() == PriceMode.FORMULA
                    ? evaluateFormulaPrice(price, product, isBuy)
                    : price.getNewPrice().orElse(Double.NaN);
            if (Double.isNaN(priceValue))
                continue;

            result.put(price.getCurrencyProvider().getId(), priceValue);
        }

        return result;
    }

    private double evaluateFormulaPrice(@NotNull Price price, @NotNull Product product, boolean isBuy) {
        final Context priceContext = price.getProperty(PriceProperty.CONTEXT);
        final Context context = new Context(ContextUtils.linkContext(
                priceContext != null ? priceContext.clone() : InternalObjectManager.GLOBAL_OBJECT,
                product.getScriptContext().clone(),
                shop.getScriptContext().clone()));
        final Script formula = price.getProperty(PriceProperty.FORMULA);

        if (formula == null) {
            HyphaShopImpl.LOGGER.warn("Formula for " + product.getId() + " is null");
            return Double.NaN;
        }

        if (formula.getLexicalScope() != null && formula.getLexicalScope().contains("bundle_total_reuse")) {
            context.declareMember("bundle_total_reuse",
                    new Reference(new Value(calculateBundleAutoReusePrice(product, isBuy)), true));
        }
        if (formula.getLexicalScope() != null && formula.getLexicalScope().contains("bundle_total_new")) {
            context.declareMember("bundle_total_new",
                    new Reference(new Value(calculateBundleAutoNewPrice(product, isBuy)), true));
        }

        final Context ctx = new VarInjector()
                .withTarget(context)
                .withRequiredVars(formula)
                .withArgs(shop, product)
                .inject();

        return ScriptUtils.evaluateDouble(ctx, formula);
    }

    private @NotNull PriceInstance calculateBundleAutoNewPrice(@NotNull Product product, boolean isBuy) {
        final PriceInstance result = new PriceInstanceImpl();
        final BundleProduct bundle = (BundleProduct) product;

        for (final Map.Entry<String, Integer> entry : bundle.getBundleContents().entrySet()) {
            final String contentId = entry.getKey();
            final Product content = HyphaShop.PRODUCT_FACTORY.getProduct(contentId);
            if (content == null) {
                if (isBuy)
                    return new PriceInstanceImpl();
                HyphaShopImpl.LOGGER.warn("Bundle product " + product.getId() + " has invalid content " + contentId);
                continue;
            }
            final List<Price> contentPrice = isBuy ? content.getBuyPrice() : content.getSellPrice();
            final PriceInstance pricePerStack = new PriceInstanceImpl(calculatePrice(contentPrice, product, isBuy));

            result.merge(pricePerStack.mul(entry.getValue()));
        }

        return result;
    }

    private @NotNull PriceInstance calculateBundleAutoReusePrice(@NotNull Product product, boolean isBuy) {
        final PriceInstance result = new PriceInstanceImpl();
        ((BundleProduct) product).getBundleContents().forEach((productId, stack) -> {
            final PriceInstance pricePerStack = isBuy ? getBuyPrice(productId) : getSellPrice(productId);
            result.merge(pricePerStack.mul(stack));
        });
        return result;
    }

    @Override
    public boolean hasCachedPrice(@NotNull String productId) {
        return cachedPrices.containsKey(productId);
    }

    @Override
    public @NotNull PricePair getModifiedPricePair(@NotNull String productId, @NotNull PricePair pricePair) {
        return pricePair;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, PricePair> getCachedPrices() {
        return Collections.unmodifiableMap(cachedPrices);
    }

    @Override
    public void setCachedPrices(@NotNull Map<String, PricePair> cachedPrices) {
        this.cachedPrices = cachedPrices;
    }

    @Override
    public @NotNull Shop getShop() {
        return shop;
    }
}
