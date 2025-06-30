package cn.encmys.ykdz.forest.hyphashop.item.builder;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.config.ShopConfig;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import cn.encmys.ykdz.forest.hyphashop.utils.DecoratorUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.MiscUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;

import java.util.*;

public class ProductIconBuilder {
    public static @NotNull Item build(@NotNull Shop shop, @NotNull Product product) {
        return build(shop, product, shop.getShopCounter().getAmount(product.getId()));
    }

    public static @NotNull Item build(@NotNull Shop shop, @NotNull Product product, int amount) {
        // 储存商品图标本身信息的 Decorator（如 base 和 desc_lore）
        // 需要与下文 staticIconDecorator 区分开
        BaseItemDecorator productIconDecorator = product.getIconDecorator();
        // 储存用于格式化商品图标的信息的 Decorator
        // 如 name，lore 和 actions 等都来自它
        // 且是静态的，用于进行条件判断以获取动态的 iconDecorator
        BaseItemDecorator staticIconDecorator = ShopConfig.getShopProductIconRecord(shop.getId()).productIconDecorator();
        Script bundleContentLine = ShopConfig.getShopProductIconRecord(shop.getId()).bundleContentLine();

        Item.Builder<?> builder = Item.builder()
                .setItemProvider((player) -> {
                    Context parent = ContextUtils.linkContext(
                            product.getScriptContext(),
                            shop.getScriptContext()
                    );

                    List<Component> bundleContentsLore = new ArrayList<>();
                    if (bundleContentLine != null && product instanceof BundleProduct) {
                        Map<String, Integer> bundleContents = ((BundleProduct) product).getBundleContents();
                        if (!bundleContents.isEmpty()) {
                            for (Map.Entry<String, Integer> entry : bundleContents.entrySet()) {
                                Product content = HyphaShop.PRODUCT_FACTORY.getProduct(entry.getKey());
                                int stack = entry.getValue();
                                if (content == null) {
                                    continue;
                                }
                                Map<String, Object> vars = new HashMap<>() {{
                                    put("stack", stack);
                                    put("total_amount", stack * amount);
                                }};
                                bundleContentsLore.add(ScriptUtils.evaluateComponent(new VarInjector()
                                        .withArg(content)
                                        .withArg(shop)
                                        .withRequiredVars(bundleContentLine)
                                        .withTarget(new Context(ContextUtils.linkContext(
                                                content.getScriptContext(),
                                                shop.getScriptContext()
                                        )))
                                        .withExtraVars(vars)
                                        .inject(), bundleContentLine));
                            }
                        }
                    }

                    // 额外变量
                    Map<String, Object> vars = Collections.unmodifiableMap(new HashMap<>() {{
                        {
                            List<Script> descLore = productIconDecorator.getProperty(ItemProperty.LORE);
                            if (descLore != null) {
                                put("desc_lore", descLore.stream()
                                        .map(lore -> ScriptUtils.evaluateComponentList(new VarInjector()
                                                .withTarget(new Context(parent))
                                                .withRequiredVars(descLore)
                                                .withArgs(product, shop)
                                                .inject(), lore))
                                        .flatMap(List::stream)
                                        .toArray(Component[]::new));
                            } else {
                                put("desc_lore", new Component[0]);
                            }
                        }
                        put("bundle_contents_lore", bundleContentsLore.toArray(new Component[0]));
                    }});

                    BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(staticIconDecorator, parent, shop, product, player);

                    return new ItemBuilder(
                            new cn.encmys.ykdz.forest.hyphashop.utils.ItemBuilder(productIconDecorator.getBaseItem().build(player))
                                    // 只有 displayName 和 lore
                                    // 会使用 iconDecorator 的设置
                                    // 其他都会继承 productIconDecorator
                                    .setDisplayName(TextUtils.parseNameToComponent(iconDecorator.getNameOrUseBaseItemName(), parent, vars, shop, product, player))
                                    .setLore(TextUtils.parseLoreToComponent(iconDecorator.getProperty(ItemProperty.LORE), parent, vars, shop, product, player))
                                    .setItemFlags(productIconDecorator.getProperty(ItemProperty.ITEM_FLAGS))
                                    .setBannerPatterns(productIconDecorator.getProperty(ItemProperty.BANNER_PATTERNS))
                                    .setFireworkEffects(productIconDecorator.getProperty(ItemProperty.FIREWORK_EFFECTS))
                                    .setEnchantments(productIconDecorator.getProperty(ItemProperty.ENCHANTMENTS))
                                    .setPotionEffects(productIconDecorator.getProperty(ItemProperty.POTION_EFFECTS))
                                    .setArmorTrim(productIconDecorator.getProperty(ItemProperty.ARMOR_TRIM))
                                    .setEnchantable(productIconDecorator.getProperty(ItemProperty.ENCHANTABLE))
                                    .setGlider(productIconDecorator.getProperty(ItemProperty.GLIDER))
                                    .setFlightDuration(productIconDecorator.getProperty(ItemProperty.FLIGHT_DURATION))
                                    .setEnchantGlint(productIconDecorator.getProperty(ItemProperty.ENCHANT_GLINT))
                                    .setPotionCustomColor(productIconDecorator.getProperty(ItemProperty.POTION_COLOR))
                                    .setPotionType(productIconDecorator.getProperty(ItemProperty.POTION_TYPE))
                                    .setPotionCustomName(productIconDecorator.getProperty(ItemProperty.POTION_CUSTOM_NAME))
                                    .setCustomModelData(productIconDecorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_FLAGS), productIconDecorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_COLORS), productIconDecorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_FLOATS), productIconDecorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_STRINGS))
                                    .build(amount));
                })
                .addClickHandler((item, click) -> {
                    Player player = click.player();

                    Context parent = ContextUtils.linkContext(
                            product.getScriptContext().clone(),
                            shop.getScriptContext().clone()
                    );

                    BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(staticIconDecorator, parent, player, shop, click, product, item);
                    ActionsConfig actions = iconDecorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, parent, Collections.emptyMap(), player, shop, click, product, item);
                });

        // 优先尊重 productIconDecorator 的更新设置
        if (Boolean.TRUE.equals(productIconDecorator.getProperty(ItemProperty.UPDATE_ON_CLICK)))
            builder.updateOnClick();
        else if (Boolean.TRUE.equals(staticIconDecorator.getProperty(ItemProperty.UPDATE_ON_CLICK)))
            builder.updateOnClick();

        Integer period = productIconDecorator.getProperty(ItemProperty.UPDATE_PERIOD) != null ?
                productIconDecorator.getProperty(ItemProperty.UPDATE_PERIOD)
                : staticIconDecorator.getProperty(ItemProperty.UPDATE_PERIOD);
        if (period != null) builder.updatePeriodically(period);

        return builder.build();
    }
}
