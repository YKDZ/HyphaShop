package cn.encmys.ykdz.forest.hyphashop.item.builder;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.utils.ItemBuilder;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProductItemBuilder {
    @NotNull
    public static ItemStack build(@NotNull Shop shop, @NotNull Product product, @Nullable Player player) {
        final BaseItemDecorator decorator = product.getProductItemDecorator();
        assert decorator != null;

        final Context parent = ContextUtils.linkContext(
                product.getScriptContext().clone(),
                shop.getScriptContext().clone()
        );

        final List<Object> args = Arrays.asList(player, shop, product);

        return new ItemBuilder(decorator.getBaseItem().build(player))
                .decorate(decorator)
                // 若没有指定 Name，则不需要为物品设置名称
                // 故不需要使用 BaseItemDecorator#getNameOrUseBaseItemName 方法
                .setDisplayName(TextUtils.parseNameToComponent((Script) decorator.getProperty(ItemProperty.NAME), parent, Collections.emptyMap(), args))
                .setLore(TextUtils.parseLoreToComponent(decorator.getProperty(ItemProperty.LORE), parent, args))
                // 构建阶段（缓存和实时）只提供一个物品
                // 具体的数量由 Product 的实现处理
                // 因为若物品采取了动态数量
                // 则 restock 后会导致缓存物品数量和实际数量不一致
                .build(1);
    }
}
