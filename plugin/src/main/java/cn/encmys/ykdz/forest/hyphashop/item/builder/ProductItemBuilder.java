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
        BaseItemDecorator decorator = product.getProductItemDecorator();
        assert decorator != null;

        Context parent = ContextUtils.linkContext(
                product.getScriptContext().clone(),
                shop.getScriptContext().clone()
        );

        List<Object> args = Arrays.asList(player, shop, product);

        return new ItemBuilder(decorator.getBaseItem().build(player))
                // 若没有指定 Name，则不需要为物品设置名称
                // 故不需要使用 BaseItemDecorator#getNameOrUseBaseItemName 方法
                .setDisplayName(TextUtils.parseNameToComponent((Script) decorator.getProperty(ItemProperty.NAME), parent, Collections.emptyMap(), args))
                .setLore(TextUtils.parseLoreToComponent(decorator.getProperty(ItemProperty.LORE), parent, args))
                .setItemFlags(decorator.getProperty(ItemProperty.ITEM_FLAGS))
                .setBannerPatterns(decorator.getProperty(ItemProperty.BANNER_PATTERNS))
                .setFireworkEffects(decorator.getProperty(ItemProperty.FIREWORK_EFFECTS))
                .setEnchantments(decorator.getProperty(ItemProperty.ENCHANTMENTS))
                .setPotionEffects(decorator.getProperty(ItemProperty.POTION_EFFECTS))
                .setArmorTrim(decorator.getProperty(ItemProperty.ARMOR_TRIM))
                .setGlider(decorator.getProperty(ItemProperty.GLIDER))
                .setFlightDuration(decorator.getProperty(ItemProperty.FLIGHT_DURATION))
                .setEnchantGlint(decorator.getProperty(ItemProperty.ENCHANT_GLINT))
                .setEnchantable(decorator.getProperty(ItemProperty.ENCHANTABLE))
                .setPotionCustomColor(decorator.getProperty(ItemProperty.POTION_COLOR))
                .setPotionType(decorator.getProperty(ItemProperty.POTION_TYPE))
                .setPotionCustomName(decorator.getProperty(ItemProperty.POTION_CUSTOM_NAME)).setCustomModelData(decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_FLAGS), decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_COLORS), decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_FLOATS), decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_STRINGS))

                // 构建阶段（缓存和实时）只提供一个物品
                // 具体的数量由 Product 的实现处理
                // 因为若物品采取了动态数量
                // 则 restock 后会导致缓存物品数量和实际数量不一致
                .build(1);
    }
}
