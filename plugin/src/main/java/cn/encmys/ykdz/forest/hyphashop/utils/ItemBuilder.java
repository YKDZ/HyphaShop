package cn.encmys.ykdz.forest.hyphashop.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final @NotNull ItemStack raw;

    public ItemBuilder(@NotNull ItemStack raw) {
        this.raw = raw;
    }

    public @NotNull ItemBuilder setDisplayName(@Nullable Component customName) {
        if (customName == null) return this;

        raw.setData(DataComponentTypes.CUSTOM_NAME, customName);

        return this;
    }

    public @NotNull ItemBuilder setLore(@Nullable List<Component> lore) {
        if (lore == null) return this;

        if (!lore.isEmpty()) {
            raw.setData(DataComponentTypes.LORE, ItemLore.lore().addLines(lore).build());
        }

        return this;
    }

    public @NotNull ItemBuilder setItemFlags(@Nullable Map<ItemFlag, Boolean> itemFlags) {
        if (itemFlags == null) return this;

        for (Map.Entry<ItemFlag, Boolean> data : itemFlags.entrySet()) {
            if (!data.getValue()) {
                raw.removeItemFlags(data.getKey());
            } else {
                raw.addItemFlags(data.getKey());
            }
        }

        return this;
    }

    public @NotNull ItemBuilder setBannerPatterns(@Nullable Map<PatternType, DyeColor> bannerPatterns) {
        if (bannerPatterns == null) return this;

        raw.setData(DataComponentTypes.BANNER_PATTERNS,
                BannerPatternLayers.bannerPatternLayers()
                        .addAll(bannerPatterns.entrySet().stream()
                                .map(entry -> new Pattern(entry.getValue(), entry.getKey()))
                                .collect(Collectors.toList()))
                        .build()
        );

        return this;
    }

    // -t:BALL -c:[#FFFFFF, #123456] -fc:[#FFFFFF, #123456] -trail:true -flicker:true
    public @NotNull ItemBuilder setFireworkEffects(@Nullable List<FireworkEffect> fireworkEffects) {
        if (fireworkEffects == null) return this;

        final Fireworks.Builder builder = Fireworks.fireworks()
                .addEffects(fireworkEffects);

        final Fireworks data = raw.getData(DataComponentTypes.FIREWORKS);
        if (data != null) {
            builder.flightDuration(data.flightDuration());
        }

        raw.setData(DataComponentTypes.FIREWORKS, builder.build());

        return this;
    }

    public @NotNull ItemBuilder setFlightDuration(@Nullable Integer flightDuration) {
        if (flightDuration == null) return this;

        final Fireworks.Builder builder = Fireworks.fireworks()
                .flightDuration(flightDuration);

        final Fireworks data = raw.getData(DataComponentTypes.FIREWORKS);
        if (data != null) {
            builder.addEffects(data.effects());
        }

        raw.setData(DataComponentTypes.FIREWORKS, builder.build());

        return this;
    }

    public @NotNull ItemBuilder setEnchantments(@Nullable Map<Enchantment, Integer> enchantments) {
        if (enchantments == null) return this;

        raw.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(enchantments));

        return this;
    }

    public @NotNull ItemBuilder setPotionEffects(@Nullable List<PotionEffect> effects) {
        if (effects == null) return this;

        final PotionContents.Builder builder = PotionContents.potionContents()
                .addCustomEffects(effects);

        final PotionContents data = raw.getData(DataComponentTypes.POTION_CONTENTS);
        if (data != null) {
            builder.potion(data.potion())
                    .customName(data.customName())
                    .customColor(data.customColor());
        }

        raw.setData(DataComponentTypes.POTION_CONTENTS, builder.build());

        return this;
    }

    public @NotNull ItemBuilder setPotionCustomColor(@Nullable Color color) {
        if (color == null) return this;

        final PotionContents.Builder builder = PotionContents.potionContents()
                .customColor(color);

        final PotionContents data = raw.getData(DataComponentTypes.POTION_CONTENTS);
        if (data != null) {
            builder.potion(data.potion())
                    .customName(data.customName())
                    .addCustomEffects(data.customEffects());
        }

        raw.setData(DataComponentTypes.POTION_CONTENTS, builder.build());

        return this;
    }

    public @NotNull ItemBuilder setPotionCustomName(@Nullable String customName) {
        if (customName == null) return this;

        final PotionContents.Builder builder = PotionContents.potionContents()
                .customName(customName);

        final PotionContents data = raw.getData(DataComponentTypes.POTION_CONTENTS);
        if (data != null) {
            builder.potion(data.potion())
                    .customColor(data.customColor())
                    .addCustomEffects(data.customEffects());
        }

        raw.setData(DataComponentTypes.POTION_CONTENTS, builder.build());

        return this;
    }

    public @NotNull ItemBuilder setPotionType(@Nullable PotionType potionType) {
        if (potionType == null) return this;

        final PotionContents.Builder builder = PotionContents.potionContents()
                .potion(potionType);

        final PotionContents data = raw.getData(DataComponentTypes.POTION_CONTENTS);
        if (data != null) {
            builder.customName(data.customName())
                    .customColor(data.customColor())
                    .addCustomEffects(data.customEffects());
        }

        raw.setData(DataComponentTypes.POTION_CONTENTS, builder.build());

        return this;
    }

    public @NotNull ItemBuilder setArmorTrim(@Nullable ArmorTrim trim) {
        if (trim == null) return this;

        raw.setData(DataComponentTypes.TRIM, ItemArmorTrim.itemArmorTrim(trim));

        return this;
    }

    public @NotNull ItemBuilder setGlider(@Nullable Boolean glider) {
        if (glider == null) return this;

        if (glider) raw.setData(DataComponentTypes.GLIDER);
        else raw.unsetData(DataComponentTypes.GLIDER);

        return this;
    }

    public @NotNull ItemBuilder setEnchantable(@Nullable Integer enchantable) {
        if (enchantable == null) return this;

        raw.setData(DataComponentTypes.ENCHANTABLE, Enchantable.enchantable(enchantable));

        return this;
    }

    public @NotNull ItemBuilder setEnchantGlint(@Nullable Boolean enchantGlint) {
        if (enchantGlint == null) return this;

        raw.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, enchantGlint);

        return this;
    }

    public @NotNull ItemBuilder setCustomModelData(@Nullable List<Boolean> flags, @Nullable List<Color> colors, @Nullable List<Float> floats, @Nullable List<String> strings) {
        final CustomModelData.Builder builder = CustomModelData.customModelData();

        if (flags != null) {
            builder.addFlags(flags);
        }
        if (colors != null) {
            builder.addColors(colors);
        }
        if (floats != null) {
            builder.addFloats(floats);
        }
        if (strings != null) {
            builder.addStrings(strings);
        }

        raw.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.build());

        return this;
    }

    public @NotNull ItemStack build(int amount) {
        raw.setAmount(amount);
        return raw;
    }
}
