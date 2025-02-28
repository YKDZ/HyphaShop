package cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums;

import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import com.google.common.reflect.TypeToken;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public enum PropertyType {
    // 通用
    NAME(new TypeToken<String>() {}),
    AMOUNT(new TypeToken<String>() {}),
    CUSTOM_MODEL_DATA(new TypeToken<Integer>() {}),
    LORE(new TypeToken<List<String>>() {}),
    ITEM_FLAGS(new TypeToken<Map<ItemFlag, Boolean>>() {}),
    ENCHANTMENTS(new TypeToken<Map<Enchantment, Integer>>() {}),
    FIREWORK_EFFECTS(new TypeToken<List<FireworkEffect>>() {}),
    BANNER_PATTERNS(new TypeToken<Map<PatternType, DyeColor>>() {}),
    POTION_EFFECTS(new TypeToken<List<PotionEffect>>() {}),
    ARMOR_TRIM(new TypeToken<ArmorTrim>() {}),
    ENCHANTABLE(new TypeToken<Integer>() {}),
    ENCHANT_GLINT(new TypeToken<Boolean>() {}),
    GLIDER(new TypeToken<Boolean>() {}),
    FLIGHT_DURATION(new TypeToken<Integer>() {}),
    POTION_TYPE(new TypeToken<PotionType>() {}),
    POTION_CUSTOM_NAME(new TypeToken<String>() {}),
    POTION_COLOR(new TypeToken<Color>() {}),
    // 按钮用
    CONDITIONAL_ICONS(new TypeToken<Map<String, BaseItemDecorator>>() {}),
    UPDATE_PERIOD(new TypeToken<Long>() {}),
    UPDATE_ON_CLICK(new TypeToken<Boolean>() {}),
    COMMANDS_DATA(new TypeToken<Map<ClickType, List<String>>>() {}),
    FEATURE_SCROLL(new TypeToken<ClickType>() {}),
    FEATURE_SCROLL_AMOUNT(new TypeToken<Integer>() {}),
    FEATURE_PAGE_CHANGE(new TypeToken<ClickType>() {}),
    FEATURE_PAGE_CHANGE_AMOUNT(new TypeToken<Integer>() {}),
    FEATURE_BACK_TO_SHOP(new TypeToken<ClickType>() {}),
    FEATURE_SETTLE_CART(new TypeToken<ClickType>() {}),
    FEATURE_OPEN_CART(new TypeToken<ClickType>() {}),
    FEATURE_SWITCH_SHOPPING_MODE(new TypeToken<ClickType>() {}),
    FEATURE_SWITCH_CART_MODE(new TypeToken<ClickType>() {}),
    FEATURE_CLEAN_CART(new TypeToken<ClickType>() {}),
    FEATURE_CLEAR_CART(new TypeToken<ClickType>() {}),
    FEATURE_LOAD_MORE_LOG(new TypeToken<ClickType>() {}),
    FEATURE_OPEN_SHOP(new TypeToken<ClickType>() {}),
    FEATURE_OPEN_SHOP_TARGET(new TypeToken<String>() {}),
    FEATURE_OPEN_ORDER_HISTORY(new TypeToken<ClickType>() {});

    @NotNull
    private final TypeToken<?> token;

    PropertyType(@NotNull TypeToken<?> token) {
        this.token = token;
    }

    public @NotNull TypeToken<?> getToken() {
        return token;
    }
}
