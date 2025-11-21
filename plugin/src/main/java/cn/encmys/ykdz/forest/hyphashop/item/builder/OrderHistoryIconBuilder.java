package cn.encmys.ykdz.forest.hyphashop.item.builder;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.gui.enums.GUIType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.config.OrderHistoryGUIConfig;
import cn.encmys.ykdz.forest.hyphashop.utils.DecoratorUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.MiscUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.*;

public class OrderHistoryIconBuilder {
    @NotNull
    public static Item build(@NotNull SettlementLog log, @NotNull Player player) {
        final BaseItemDecorator historyStaticIcon = OrderHistoryGUIConfig.getHistoryIconRecord().iconDecorator();
        final BaseItemDecorator historyPlaceholderIcon = OrderHistoryGUIConfig.getHistoryIconRecord().miscPlaceholderIcon();
        final Script orderContentLine = OrderHistoryGUIConfig.getHistoryIconRecord().formatOrderContentLine();

        assert orderContentLine != null;

        return Item.builder()
                .async(ItemProvider.EMPTY, () -> {
                    // 构造内部列表变量
                    final Map<String, Object> vars = new HashMap<>() {{
                        final List<Object> orderContentsLines = new ArrayList<>();
                        for (Map.Entry<ProductLocation, AmountPair> entry : log.getOrderedProducts().entrySet()) {
                            final ProductLocation productLoc = entry.getKey();
                            final Shop shop = productLoc.shop();

                            if (shop == null) continue;

                            final AmountPair amountPair = entry.getValue();
                            final Product product = productLoc.product();
                            final Context parent;
                            if (product == null) {
                                parent = ContextUtils.linkContext(
                                        shop.getScriptContext()
                                );
                            } else {
                                parent = ContextUtils.linkContext(
                                        product.getScriptContext(),
                                        shop.getScriptContext()
                                );
                            }
                            orderContentsLines.add(ScriptUtils.evaluateComponent(new VarInjector()
                                            .withRequiredVars(orderContentLine)
                                            .withExtraVars(new HashMap<>() {{
                                                put("stack", amountPair.stack());
                                                put("amount", amountPair.amount());
                                            }})
                                            .withTarget(new Context(parent))
                                            .withArg(product)
                                            .inject()
                                    , orderContentLine));
                        }
                        put("order_contents", orderContentsLines.toArray());
                    }};

                    // 构建显示物品

                    final ItemStack displayItem = log.getOrderedProducts().keySet().stream()
                            // 其他插件的物品不保证在异步环境下能被构建
                            // 例如 MMOItems
                            .filter(productLoc -> {
                                final Product product = productLoc.product();
                                if (product == null) return false;
                                return product.getIconDecorator().getBaseItem().getItemType().isAsyncBuildable();
                            })
                            .findFirst()
                            .map(productLoc -> {
                                final Shop shop = productLoc.shop();
                                final Product product = productLoc.product();
                                if (product == null || shop == null) return new ItemStack(Material.AIR);
                                return ProductIconBuilder.build(shop, product, log.getOrderedProducts().get(productLoc).amount()).getItemProvider(player).get();
                            }).orElse(null);

                    if (displayItem == null || displayItem.getType() == Material.AIR) {
                        return NormalIconBuilder.build(historyPlaceholderIcon, GUIType.NORMAL, InternalObjectManager.GLOBAL_OBJECT, player, log)
                                .getItemProvider(player);
                    }

                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(historyStaticIcon, InternalObjectManager.GLOBAL_OBJECT, log, player);

                    return new ItemBuilder(new cn.encmys.ykdz.forest.hyphashop.utils.ItemBuilder(displayItem)
                            .setDisplayName(TextUtils.parseNameToComponent(iconDecorator.getNameOrUseBaseItemName(), InternalObjectManager.GLOBAL_OBJECT, vars, log, player))
                            .setLore(TextUtils.parseLoreToComponent(iconDecorator.getProperty(ItemProperty.LORE), InternalObjectManager.GLOBAL_OBJECT, vars, log, player))
                            .setItemFlags(iconDecorator.getProperty(ItemProperty.ITEM_FLAGS))
                            .build(log.getOrderedProducts().size())
                    );
                })
                .addClickHandler((item, click) -> {
                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(historyStaticIcon, InternalObjectManager.GLOBAL_OBJECT, item, click, click.player());
                    final ActionsConfig actions = iconDecorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, InternalObjectManager.GLOBAL_OBJECT, Collections.emptyMap(), item, click, click.player());
                })
                .build();
    }
}
