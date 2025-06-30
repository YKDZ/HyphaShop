package cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums;

import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.gui.record.ConditionalIconRecord;
import com.google.common.reflect.TypeToken;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public enum ItemProperty {
    // 通用
    NAME(new TypeToken<Script>() {
    }),
    AMOUNT(new TypeToken<Script>() {
    }),
    LORE(new TypeToken<List<Script>>() {
    }),
    ITEM_FLAGS(new TypeToken<Map<ItemFlag, Boolean>>() {
    }),
    ENCHANTMENTS(new TypeToken<Map<Enchantment, Integer>>() {
    }),
    FIREWORK_EFFECTS(new TypeToken<List<FireworkEffect>>() {
    }),
    BANNER_PATTERNS(new TypeToken<Map<PatternType, DyeColor>>() {
    }),
    POTION_EFFECTS(new TypeToken<List<PotionEffect>>() {
    }),
    ARMOR_TRIM(new TypeToken<ArmorTrim>() {
    }),
    ENCHANTABLE(new TypeToken<Integer>() {
    }),
    ENCHANT_GLINT(new TypeToken<Boolean>() {
    }),
    GLIDER(new TypeToken<Boolean>() {
    }),
    FLIGHT_DURATION(new TypeToken<Integer>() {
    }),
    POTION_TYPE(new TypeToken<PotionType>() {
    }),
    POTION_CUSTOM_NAME(new TypeToken<String>() {
    }),
    POTION_COLOR(new TypeToken<Color>() {
    }),
    CUSTOM_MODEL_DATA_FLAGS(new TypeToken<List<Boolean>>() {
    }),
    CUSTOM_MODEL_DATA_COLORS(new TypeToken<List<Color>>() {
    }),
    CUSTOM_MODEL_DATA_FLOATS(new TypeToken<List<Float>>() {
    }),
    CUSTOM_MODEL_DATA_STRINGS(new TypeToken<List<String>>() {
    }),
    // 按钮用
    UPDATE_PERIOD(new TypeToken<Integer>() {
    }),
    UPDATE_ON_CLICK(new TypeToken<Boolean>() {
    }),
    ACTIONS(new TypeToken<ActionsConfig>() {
    }),
    CONDITIONAL_ICONS(new TypeToken<List<ConditionalIconRecord>>() {
    });

    @NotNull
    private final TypeToken<?> token;

    ItemProperty(@NotNull TypeToken<?> token) {
        this.token = token;
    }

    public @NotNull TypeToken<?> getToken() {
        return token;
    }
}
