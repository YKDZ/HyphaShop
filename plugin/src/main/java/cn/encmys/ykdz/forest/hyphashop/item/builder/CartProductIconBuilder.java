package cn.encmys.ykdz.forest.hyphashop.item.builder;

import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.profile.cart.Cart;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.config.CartGUIConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.DecoratorUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ItemBuilder;
import cn.encmys.ykdz.forest.hyphashop.utils.MiscUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.Item;

import java.util.Collections;
import java.util.Map;

public class CartProductIconBuilder {
    @NotNull
    public static Item build(@NotNull Cart cart, @NotNull ProductLocation productLoc) {
        final Product product = productLoc.product();
        final Shop shop = productLoc.shop();

        if (product == null || shop == null) return Item.simple(new ItemStack(Material.AIR));

        final BaseItemDecorator staticIconDecorator = CartGUIConfig.getCartProductIconRecord().iconDecorator();

        var builder = Item.builder()
                .setItemProvider((player) -> {
                    final int stack = cart.getOrder().getOrderedProducts().getOrDefault(productLoc, 0);
                    if (stack <= 0) {
                        return new xyz.xenondevs.invui.item.ItemBuilder(Material.AIR);
                    }

                    final Map<String, Object> vars = Map.of(
                            "stack", stack,
                            "total_price", cart.getOrder().getBilledPrice(productLoc)
                    );

                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(staticIconDecorator, ContextUtils.linkContext(
                            product.getScriptContext().clone(),
                            shop.getScriptContext().clone()
                    ), player, shop, product, cart.getOrder());

                    // 在商品自己图标的基础上覆盖名称、lore 和 itemFlags
                    return new xyz.xenondevs.invui.item.ItemBuilder(
                            new ItemBuilder(product.getIconDecorator().getBaseItem().build(player))
                                    .decorate(product.getIconDecorator())
                                    .setDisplayName(TextUtils.parseNameToComponent(iconDecorator.getNameOrUseBaseItemName(), ContextUtils.linkContext(
                                            product.getScriptContext().clone(),
                                            shop.getScriptContext().clone()
                                    ), vars, player, shop, product, cart.getOrder()))
                                    .setLore(TextUtils.parseLoreToComponent(iconDecorator.getProperty(ItemProperty.LORE), ContextUtils.linkContext(
                                            product.getScriptContext().clone(),
                                            shop.getScriptContext().clone()
                                    ), vars, player, shop, product, cart.getOrder()))
                                    .build(stack)
                    );
                })
                .addClickHandler((item, click) -> {
                    final Player player = click.player();

                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(staticIconDecorator, ContextUtils.linkContext(
                            product.getScriptContext().clone(),
                            shop.getScriptContext().clone()
                    ), player, shop, product, click, item, cart.getOrder());
                    final ActionsConfig actions = iconDecorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, ContextUtils.linkContext(
                            product.getScriptContext(),
                            shop.getScriptContext()
                    ), Collections.emptyMap(), player, shop, product, click, item, cart.getOrder());
                });

        if (Boolean.TRUE.equals(staticIconDecorator.getProperty(ItemProperty.UPDATE_ON_CLICK))) builder.updateOnClick();
        final Integer period = staticIconDecorator.getProperty(ItemProperty.UPDATE_PERIOD);
        if (period != null) builder.updatePeriodically(period);
        return builder.build();
    }
}
