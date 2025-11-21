package cn.encmys.ykdz.forest.hyphashop.shop.pricer;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.price.Price;
import cn.encmys.ykdz.forest.hyphashop.api.price.PricePair;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceMode;
import cn.encmys.ykdz.forest.hyphashop.api.price.enums.PriceProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.pricer.ShopPricer;
import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphashop.price.PricePairImpl;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import cn.encmys.ykdz.forest.hyphashop.shop.ShopImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ShopPricerImpl implements ShopPricer {
    private final @NotNull Shop shop;
    private @NotNull Map<String, PricePair> cachedPrices = new HashMap<>();

    public ShopPricerImpl(@NotNull ShopImpl shop) {
        this.shop = shop;
    }

    @Override
    public double getBuyPrice(@NotNull String productId) {
        return getPrice(productId, PricePair::getBuy, "buy");
    }

    @Override
    public double getSellPrice(@NotNull String productId) {
        return getPrice(productId, PricePair::getSell, "sell");
    }

    private double getPrice(@NotNull String productId, @NotNull Function<PricePair, Double> extractor, @NotNull String priceType) {
        final PricePair pricePair = cachedPrices.get(productId);
        if (pricePair == null) {
            LogUtils.warn("Product " + productId + " do not have " + priceType + "-price cached. This is likely a bug.");
            return Double.NaN;
        }
        return extractor.apply(pricePair);
    }

    @Override
    public boolean cachePrice(@NotNull String productId) {
        final Product product = HyphaShop.PRODUCT_FACTORY.getProduct(productId);
        if (product == null) {
            LogUtils.warn("Try to cache price for product " + productId + " which does not exist.");
            return false;
        }

        double buy = calculatePrice(product.getBuyPrice(), product, true);
        double sell = calculatePrice(product.getSellPrice(), product, false);

        if (!Double.isNaN(buy) && buy <= sell) {
            handlePriceConflict(productId, buy, sell);
            if (Config.priceCorrectByDisableSellOrBuy) sell = Double.NaN;
            else buy = Double.NaN;
        }

        if (Double.isNaN(buy) && Double.isNaN(sell)) {
            LogUtils.warn("Price cache for " + productId + " failed cause both buy and sell are invalid.");
            return false;
        }

        cachedPrices.put(productId, getModifiedPricePair(productId, new PricePairImpl(buy, sell)));
        return true;
    }

    private double calculatePrice(@NotNull Price price, @NotNull Product product, boolean isBuy) {
        if (price.getPriceMode() == PriceMode.FORMULA) return evaluateFormulaPrice(price, product, isBuy);
        else return price.getNewPrice();
    }

    private double evaluateFormulaPrice(@NotNull Price price, @NotNull Product product, boolean isBuy) {
        final Context priceContext = price.getProperty(PriceProperty.CONTEXT);
        final Context context = new Context(ContextUtils.linkContext(
                priceContext != null ? priceContext.clone() : InternalObjectManager.GLOBAL_OBJECT,
                product.getScriptContext().clone(),
                shop.getScriptContext().clone()
        ));
        final Script formula = price.getProperty(PriceProperty.FORMULA);

        if (formula == null) {
            LogUtils.warn("Formula for " + product.getId() + " is null");
            return Double.NaN;
        }

        if (formula.getLexicalScope() != null && formula.getLexicalScope().contains("bundle_total_reuse")) {
            context.declareMember("bundle_total_reuse", new Reference(new Value(calculateBundleAutoReusePrice(product, isBuy)), true));
        }
        if (formula.getLexicalScope() != null && formula.getLexicalScope().contains("bundle_total_new")) {
            context.declareMember("bundle_total_new", new Reference(new Value(calculateBundleAutoNewPrice(product, isBuy)), true));
        }

        final Context ctx = new VarInjector()
                .withTarget(context)
                .withRequiredVars(formula)
                .withArgs(shop, product)
                .inject();

        return ScriptUtils.evaluateDouble(ctx, formula);
    }

    private double calculateBundleAutoNewPrice(@NotNull Product product, boolean isBuy) {
        double total = 0;
        final BundleProduct bundle = (BundleProduct) product;
        for (final Map.Entry<String, Integer> entry : bundle.getBundleContents().entrySet()) {
            final String contentId = entry.getKey();
            final Product content = HyphaShop.PRODUCT_FACTORY.getProduct(contentId);
            if (content == null) {
                if (isBuy) return Double.NaN;
                LogUtils.warn("Bundle product " + product.getId() + " has invalid content " + contentId);
                continue;
            }
            final Price contentPrice = isBuy ? content.getBuyPrice() : content.getSellPrice();
            total += contentPrice.getNewPrice() * entry.getValue();
        }
        return total;
    }

    private double calculateBundleAutoReusePrice(@NotNull Product product, boolean isBuy) {
        return ((BundleProduct) product).getBundleContents().entrySet().stream()
                .mapToDouble(entry -> {
                    double price = isBuy ? getBuyPrice(entry.getKey()) : getSellPrice(entry.getKey());
                    return price * entry.getValue();
                })
                .sum();
    }

    private void handlePriceConflict(@NotNull String productId, double buy, double sell) {
        final String msg = String.format(
                "Buy price (%.2f) <= sell price (%.2f) for %s. Disabling %s.",
                buy, sell, productId, Config.priceCorrectByDisableSellOrBuy ? "sell" : "buy"
        );
        LogUtils.warn(msg);
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
