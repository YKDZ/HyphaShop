package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.JavaFunction;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.gui.enums.GUIType;
import cn.encmys.ykdz.forest.hyphashop.api.gui.record.ConditionalIconRecord;
import cn.encmys.ykdz.forest.hyphashop.api.item.BaseItem;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.item.builder.BaseItemBuilder;
import cn.encmys.ykdz.forest.hyphashop.item.builder.NormalIconBuilder;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigInheritor;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.xenondevs.invui.gui.Gui;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConfigUtils {
    /**
     * @param data Trim data format like "diamond:vex"
     * @return ArmorTrim
     */
    public static @Nullable ArmorTrim parseArmorTrimData(@Nullable String data) {
        if (data == null) return null;

        final String[] parsed = data.split(":");

        if (!Key.parseable(parsed[0])) {
            LogUtils.warn("Invalid namespace config: " + parsed[0] + " in " + data);
            return null;
        }

        if (!Key.parseable(parsed[1])) {
            LogUtils.warn("Invalid namespace config: " + parsed[1] + " in " + data);
            return null;
        }

        try {
            Registry<@NotNull TrimMaterial> materialRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL);
            Registry<@NotNull TrimPattern> patternRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
            return new ArmorTrim(
                    materialRegistry.getOrThrow(Key.key(parsed[0])),
                    patternRegistry.getOrThrow(Key.key(parsed[1]))
            );
        } catch (Throwable ignored) {
            LogUtils.warn("Format of armor data: " + data + " is invalid. Use diamond:vex as fallback.");
            return new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.VEX);
        }
    }

    /**
     * @param data List of potion effect data format like "night_vision:100:1:true:true:true" (PotionEffectType:duration:amplifier:ambient:particles:icon)
     * @return Enchantment and Level
     */
    public static @Nullable List<Color> parseColorsData(@NotNull List<String> data) {
        if (data.isEmpty()) return null;
        return data.stream()
                .map(ConfigUtils::parseColorData)
                .collect(Collectors.toList());
    }

    /**
     * @param data Hex color or enum field name of Color
     * @return Color
     */
    public static @Nullable Color parseColorData(@Nullable String data) {
        if (data == null) return null;

        Color color = Color.GREEN;
        if (data.startsWith("#")) {
            try {
                color = Color.fromRGB(Integer.parseInt(data.substring(1), 16));
            } catch (IllegalArgumentException e) {
                LogUtils.warn("Invalid color definition: " + data + ". Use green as fallback color.");
            }
        } else {
            try {
                return (Color) Color.class.getField(data).get(null);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                LogUtils.warn("Invalid color definition: " + data + ". Use green as fallback color.");
            }
        }
        return color;
    }

    /**
     * @param data List of potion effect data format like "night_vision:100:1:true:true:true" (PotionEffectType:duration:amplifier:ambient:particles:icon)
     * @return Enchantment and Level
     */
    public static @Nullable List<PotionEffect> parsePotionEffectsData(@NotNull List<String> data) {
        if (data.isEmpty()) return null;
        return data.stream()
                .map(ConfigUtils::parsePotionEffectsData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @param data Potion effect data format like "night_vision:100:1:true:true:true" (PotionEffectType:duration:amplifier:ambient:particles:icon)
     * @return PotionEffect
     */
    private static @Nullable PotionEffect parsePotionEffectsData(@NotNull String data) {
        final String[] parsed = data.split(":");

        if (!Key.parseable(parsed[0])) {
            LogUtils.warn("Invalid namespace config: " + parsed[0] + " in " + data);
            return null;
        }

        try {
            Registry<@NotNull PotionEffectType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT);
            return new PotionEffect(
                    registry.getOrThrow(Key.key(parsed[0])),
                    Integer.parseInt(parsed[1]),
                    Integer.parseInt(parsed[2]),
                    Boolean.parseBoolean(parsed[3]),
                    Boolean.parseBoolean(parsed[4]),
                    Boolean.parseBoolean(parsed[5])
            );
        } catch (Throwable ignored) {
            LogUtils.warn("Potion effect data: " + data + " is invalid. Use night_vision:100:1:true:true:true as fallback.");
            return new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 1, true, true, true);
        }
    }

    /**
     * @param data List of enchantment data format like "sharpness:5" or "knockback"
     * @return Enchantment and Level
     */
    public static @Nullable Map<Enchantment, Integer> parseEnchantmentData(@NotNull List<String> data) {
        if (data.isEmpty()) return null;
        return data.stream()
                .map(ConfigUtils::parseEnchantmentData)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * @param data Enchantment data format like "sharpness:5"
     * @return Enchantment and Level
     */
    private static @NotNull Map.Entry<Enchantment, Integer> parseEnchantmentData(@NotNull String data) {
        final String[] parsed = data.split(":");

        if (!Key.parseable(parsed[0])) {
            LogUtils.warn("Invalid namespace config: " + parsed[0] + " in " + data);
            return Map.entry(Enchantment.SHARPNESS, 5);
        }

        try {
            final Registry<@NotNull Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
            final Enchantment enchantment = enchantmentRegistry.getOrThrow(Key.key(parsed[0]));
            int level = Integer.parseInt(parsed[1]);

            return Map.entry(enchantment, level);
        } catch (Throwable ignored) {
            LogUtils.warn("Enchantment data: " + data + " is invalid. Use sharpness:5 as fallback");
            return Map.entry(Enchantment.SHARPNESS, 5);
        }
    }

    /**
     * @param data List of banner pattern data format like "YELLOW:bricks"
     * @return Pattern type and its color
     */
    public static @Nullable Map<PatternType, DyeColor> parseBannerPatternData(@NotNull List<String> data) {
        if (data.isEmpty()) return null;

        // 列表中靠上的图案在底层（先被绘制）
        return data.stream()
                .map(ConfigUtils::parseBannerPatternData)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * @param data Banner pattern data format like "YELLOW:bricks"
     * @return Pattern type and its color
     */
    private static @NotNull Map.Entry<PatternType, DyeColor> parseBannerPatternData(@NotNull @Subst("YELLOW:bricks") String data) {
        final String[] parsed = data.split(":");

        if (!Key.parseable(parsed[1])) {
            LogUtils.warn("Invalid namespace config: " + parsed[1] + " in " + data);
            return Map.entry(PatternType.BRICKS, DyeColor.YELLOW);
        }

        try {
            final DyeColor color = DyeColor.valueOf(parsed[0]);
            final Registry<@NotNull PatternType> bannerPatternRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN);
            final PatternType type = bannerPatternRegistry.getOrThrow(Key.key(parsed[1]));

            return Map.entry(type, color);
        } catch (NoSuchElementException | InvalidKeyException e) {
            LogUtils.warn("Banner pattern data: " + data + " is invalid. Use YELLOW:bricks as fallback");
            return Map.entry(PatternType.BRICKS, DyeColor.YELLOW);
        }
    }

    /**
     * @param data List of item flag data format like "HIDE_POTION_EFFECTS" or "-HIDE_ATTRIBUTES"
     * @return ItemFlag: isAdd
     */
    public static @Nullable Map<ItemFlag, Boolean> parseItemFlagData(@NotNull List<String> data) {
        if (data.isEmpty()) return null;
        return data.stream()
                .map(ConfigUtils::parseItemFlagData)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * @param data Item flag data format like "HIDE_ADDITIONAL_TOOLTIP"
     * @return ItemFlag: isAdd
     */
    private static Map.@NotNull @Unmodifiable Entry<ItemFlag, Boolean> parseItemFlagData(@NotNull String data) {
        try {
            final ItemFlag flag = ItemFlag.valueOf(data.replaceAll("-", ""));
            final boolean isAdd = !data.startsWith("-");
            return Map.entry(flag, isAdd);
        } catch (IllegalArgumentException e) {
            LogUtils.warn("Banner pattern data: " + data + " is invalid. Use HIDE_ADDITIONAL_TOOLTIP as fallback");
            return Map.entry(ItemFlag.HIDE_DYE, true);
        }
    }

    /**
     * @param data List of firework effect data format like "-t:BALL -c:[#FFFFFF, #123456] -fc:[#FFFFFF, #123456] -trail:true -flicker:true"
     * @return Firework effects
     */
    public static @Nullable List<FireworkEffect> parseFireworkEffectData(@NotNull List<String> data) {
        if (data.isEmpty()) return null;
        return data.stream().map(ConfigUtils::parseFireworkEffectData).collect(Collectors.toList());
    }

    /**
     * @param data Firework effect data format like "-t:BALL -c:[#FFFFFF, #123456] -fc:[#FFFFFF, #123456] -trail:true -flicker:true"
     * @return Firework effect
     */
    private static @NotNull FireworkEffect parseFireworkEffectData(@NotNull String data) {
        final Map<String, String> params = new HashMap<>();
        final Map<String, List<String>> listParams = new HashMap<>();

        final Pattern p = java.util.regex.Pattern.compile("-(\\w+):(\\[.*?]|\\w+)");
        final Matcher m = p.matcher(data);

        while (m.find()) {
            final String key = m.group(1);
            final String value = m.group(2);

            if (value.startsWith("[")) {
                String[] listValues = value.substring(1, value.length() - 1).split(",\\s*");
                listParams.put(key, Arrays.asList(listValues));
            } else {
                params.put(key, value);
            }
        }

        final List<Color> colors = new ArrayList<>();
        final List<Color> fadeColors = new ArrayList<>();

        for (final String hex : listParams.get("c")) {
            colors.add(ColorUtils.getFromHex(hex));
        }

        for (final String hex : listParams.get("fc")) {
            fadeColors.add(ColorUtils.getFromHex(hex));
        }

        return FireworkEffect.builder()
                .with(FireworkEffect.Type.valueOf(params.getOrDefault("t", "BALL")))
                .withColor(colors)
                .withFade(fadeColors)
                .flicker(Boolean.parseBoolean(params.getOrDefault("flicker", "false")))
                .trail(Boolean.parseBoolean(params.getOrDefault("trail", "false")))
                .build();
    }

    public static @Nullable PotionType parsePotionTypeData(@Nullable String data) {
        if (data == null) return null;

        if (!Key.parseable(data)) {
            LogUtils.warn("Invalid namespace config: " + data);
            return null;
        }

        Registry<@NotNull PotionType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.POTION);

        PotionType type = PotionType.HEALING;
        try {
            type = registry.getOrThrow(Key.key(data));
        } catch (NoSuchElementException | InvalidKeyException ignored) {
            LogUtils.warn("Invalid potion type data: " + data + ". Use HEALING as fallback");
        }

        return type;
    }

    public static @NotNull Map<Character, BaseItemDecorator> parseIconDecorators(@NotNull ConfigAccessor config) {
        final Map<Character, BaseItemDecorator> icons = new HashMap<>();
        config.getLocalMembers().orElse(new HashMap<>()).forEach((key, childConfig) -> icons.put(key.charAt(0), parseDecorator(childConfig)));
        return icons;
    }

    public static @NotNull Gui parseGui(@NotNull ConfigAccessor config) {
        final String[] structure = config.getStringList("structure").orElse(new ArrayList<>()).toArray(new String[0]);
        final ConfigAccessor icons = config.getConfig("icons").orElse(null);

        if (icons == null) return Gui.builder().build();

        final Gui.Builder<?, ?> builder = Gui.builder()
                .setStructure(structure);

        icons.getLocalMembers().orElse(new HashMap<>()).forEach((key, childConfig) -> builder.addIngredient(key.charAt(0), NormalIconBuilder.build(parseDecorator(childConfig), GUIType.NORMAL, InternalObjectManager.GLOBAL_OBJECT)));

        return builder.build();
    }

    public static @NotNull BaseItemDecorator parseDecorator(@NotNull ConfigAccessor config) {
        final String base = config.getString("base").orElse("dirt");
        final BaseItem item = BaseItemBuilder.get(base);

        return new BaseItemDecorator(item)
                .setProperty(ItemProperty.NAME, StringUtils.wrapToScriptWithOmit(config.getString("name").orElse(null)).orElse(null))
                .setProperty(ItemProperty.LORE, StringUtils.wrapToScriptWithOmit(config.getStringList("lore").orElse(null)))
                .setProperty(ItemProperty.AMOUNT, StringUtils.wrapToScript(config.getString("amount").orElse("1")))
                .setProperty(ItemProperty.UPDATE_PERIOD, TextUtils.parseTimeStringToTicks(config.getString("update-period").orElse(null)))
                .setProperty(ItemProperty.UPDATE_ON_CLICK, config.getBoolean("update-on-click").orElse(false))
                .setProperty(ItemProperty.ITEM_FLAGS, parseItemFlagData(config.getStringList("item-flags").orElse(new ArrayList<>())))
                .setProperty(ItemProperty.BANNER_PATTERNS, parseBannerPatternData(config.getStringList("banner-patterns").orElse(new ArrayList<>())))
                .setProperty(ItemProperty.ENCHANTMENTS, parseEnchantmentData(config.getStringList("enchantments").orElse(new ArrayList<>())))
                .setProperty(ItemProperty.FIREWORK_EFFECTS, parseFireworkEffectData(config.getStringList("firework-effects").orElse(new ArrayList<>())))
                .setProperty(ItemProperty.POTION_EFFECTS, parsePotionEffectsData(config.getStringList("potion-effects").orElse(new ArrayList<>())))
                .setProperty(ItemProperty.ARMOR_TRIM, parseArmorTrimData(config.getString("armor-trim").orElse(null)))
                .setProperty(ItemProperty.ENCHANT_GLINT, config.getBoolean("enchantment-glint").orElse(null))
                .setProperty(ItemProperty.ENCHANTABLE, config.getBoolean("enchantable").orElse(null))
                .setProperty(ItemProperty.GLIDER, config.getBoolean("glider").orElse(null))
                .setProperty(ItemProperty.UNBREAKABLE, config.getBoolean("unbreakable").orElse(null))
                .setProperty(ItemProperty.FLIGHT_DURATION, config.getInt("flight-duration").orElse(null))
                .setProperty(ItemProperty.POTION_TYPE, parsePotionTypeData(config.getString("potion-type").orElse(null)))
                .setProperty(ItemProperty.POTION_COLOR, parseColorData(config.getString("potion-color").orElse(null)))
                .setProperty(ItemProperty.POTION_CUSTOM_NAME, config.getComponent("potion-custom-name").orElse(null))
                .setProperty(ItemProperty.CUSTOM_MODEL_DATA_FLAGS, config.getBooleanList("custom-model-data.flags").orElse(null))
                .setProperty(ItemProperty.CUSTOM_MODEL_DATA_COLORS, parseColorsData(config.getStringList("custom-model-data.colors").orElse(new ArrayList<>())))
                .setProperty(ItemProperty.CUSTOM_MODEL_DATA_FLOATS, config.getFloatList("custom-model-data.floats").orElse(null))
                .setProperty(ItemProperty.CUSTOM_MODEL_DATA_STRINGS, config.getStringList("custom-model-data.strings").orElse(null))
                .setProperty(ItemProperty.TOOLTIP_DISPLAY_HIDE_TOOLTIP, config.getBoolean("tooltip-display.hide-tooltip").orElse(null))
                .setProperty(ItemProperty.CONDITIONAL_ICONS, parseConditionIconRecords(config.getConfigList("icons").orElse(new ArrayList<>()), config))
                .setProperty(ItemProperty.ACTIONS, ActionsConfig.of(config.getConfig("actions").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()))));
    }

    public static @NotNull List<ConditionalIconRecord> parseConditionIconRecords(@NotNull List<? extends ConfigAccessor> configList, @NotNull ConfigAccessor parent) {
        final List<ConditionalIconRecord> conditionIcons = new ArrayList<>();

        IntStream.range(0, configList.size()).forEach(i -> {
            final ConfigAccessor config = configList.get(i);

            final String conditionStr = config.getString("condition").orElse(null);
            if (conditionStr == null) return;

            final Script condition = StringUtils.wrapToScript(conditionStr);

            if (condition == null) return;

            ConfigAccessor icon = config.getConfig("icon").orElse(null);
            if (icon == null) return;

            final ActionsConfig parentActions = ActionsConfig.of(parent.getConfig("actions").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));
            final ActionsConfig actions = ActionsConfig.of(icon.getConfig("actions").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));

            if (config.getBoolean("inherit").orElse(true)) {
                // 手动继承 actions
                actions.inherit(parentActions);
                icon = new ConfigInheritor(parent, icon);
            }

            // 索引小的优先级高
            final int priority = config.getInt("priority").orElse(configList.size() - i);

            final BaseItemDecorator iconDecorator = parseDecorator(icon)
                    .setProperty(ItemProperty.ACTIONS, actions);

            conditionIcons.add(new ConditionalIconRecord(
                    condition,
                    priority,
                    iconDecorator
            ));
        });

        return conditionIcons;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull Dialog parseDialog(@NotNull ConfigAccessor config) {
        final Component title = config.getComponent("title").orElse(Component.empty());
        final boolean canCloseWithEscape = config.getBoolean("closeWithEscape").orElse(true);
        final List<? extends ConfigAccessor> inputs = config.getConfigList("inputs").orElse(List.of());
        final List<? extends ConfigAccessor> actions = config.getConfigList("actions").orElse(List.of());

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title).canCloseWithEscape(canCloseWithEscape).inputs(inputs.stream().map(inputConfig ->
                        switch (inputConfig.getString("type").orElse("text")) {
                            case "single-option" -> DialogInput.singleOption(
                                    inputConfig.getString("key").orElse(UUID.randomUUID().toString()),
                                    inputConfig.getInt("width").orElse(1024),
                                    inputConfig.getConfigList("options").orElse(List.of()).stream().map((optionConfig) -> SingleOptionDialogInput.OptionEntry.create(
                                            optionConfig.getString("id").orElse(UUID.randomUUID().toString()),
                                            optionConfig.getComponent("display").orElse(Component.text("Option")),
                                            optionConfig.getBoolean("initial").orElse(false)
                                    )).toList(),
                                    inputConfig.getComponent("label").orElse(Component.empty()),
                                    true
                            );
                            case "number-range" -> DialogInput.numberRange(
                                    inputConfig.getString("key").orElse(UUID.randomUUID().toString()),
                                    inputConfig.getInt("width").orElse(1024),
                                    inputConfig.getComponent("label").orElse(Component.empty()),
                                    inputConfig.getString("labelFormat").orElse(""),
                                    inputConfig.getFloat("start").orElse(0f),
                                    inputConfig.getFloat("end").orElse(0f),
                                    inputConfig.getFloat("initial").orElse(0f),
                                    inputConfig.getFloat("step").orElse(0f)
                            );
                            case "boolean" -> DialogInput.bool(
                                    inputConfig.getString("key").orElse(UUID.randomUUID().toString()),
                                    inputConfig.getComponent("label").orElse(Component.empty())
                            ).build();
                            default -> DialogInput.text(
                                    inputConfig.getString("key").orElse(UUID.randomUUID().toString()),
                                    inputConfig.getInt("width").orElse(1024),
                                    inputConfig.getComponent("label").orElse(Component.empty()),
                                    true,
                                    inputConfig.getString("initial").orElse(""),
                                    inputConfig.getInt("maxLength").orElse(64),
                                    TextDialogInput.MultilineOptions.create(
                                            inputConfig.getInt("maxLines").orElse(1),
                                            inputConfig.getInt("maxHeight").orElse(64)
                                    )
                            );
                        }).toList()).build())
                .type(DialogType.multiAction(
                                actions.stream().map(actionConfig ->
                                {
                                    final ConfigAccessor clickConfig = actionConfig.getConfig("click").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()));
                                    return ActionButton.create(
                                            actionConfig.getComponent("label").orElse(Component.empty()),
                                            actionConfig.getComponent("tooltip").orElse(Component.empty()),
                                            actionConfig.getInt("width").orElse(1024),
                                            switch (clickConfig.getString("type").orElse("static")) {
                                                case "custom" -> DialogAction.customClick(
                                                        (view, audience) -> {
                                                            final Context ctx = Context.Builder.create()
                                                                    .with("getText", new Value(new JavaFunction("getText",
                                                                            (target, paras, context) ->
                                                                                    view.getText(paras.getFirst().getAsString()))))
                                                                    .with("getFloat", new Value(new JavaFunction("getFloat",
                                                                            (target, paras, context) ->
                                                                                    view.getFloat(paras.getFirst().getAsString()))))
                                                                    .with("getBoolean", new Value(new JavaFunction("getBoolean",
                                                                            (target, paras, context) ->
                                                                                    view.getBoolean(paras.getFirst().getAsString()))))
                                                                    .build();
                                                            clickConfig.getFunction("onClick", ctx)
                                                                    .ifPresent(onClick -> onClick.call(new Value(), List.of(), ctx));
                                                        },
                                                        ClickCallback.Options.builder()
                                                                .uses(1)
                                                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                                                .build()
                                                );
                                                case "command" -> DialogAction.commandTemplate(
                                                        clickConfig.getString("template").orElse("")
                                                );
                                                default -> DialogAction.staticAction(
                                                        switch (clickConfig.getString("type").orElse("copy")) {
                                                            case "copy" ->
                                                                    ClickEvent.copyToClipboard(clickConfig.getString("copied").orElse("copied text"));
                                                            case "run-command" ->
                                                                    ClickEvent.runCommand(clickConfig.getString("command").orElse("/say Custom Command"));
                                                            case "open-url" ->
                                                                    ClickEvent.openUrl(clickConfig.getString("url").orElse("https://github.com/YKDZ"));
                                                            case "open-file" ->
                                                                    ClickEvent.openFile(clickConfig.getString("file").orElse("file.txt"));
                                                            case "change-page" ->
                                                                    ClickEvent.changePage(clickConfig.getInt("page").orElse(0));
                                                            case "open-dialog" ->
                                                                    ClickEvent.showDialog(parseDialog(clickConfig.getConfig("dialog").orElse(new ConfigurationSectionAccessor(new YamlConfiguration()))));
                                                            default ->
                                                                    ClickEvent.suggestCommand(clickConfig.getString("command").orElse("/suggested command"));
                                                        }
                                                );
                                            }
                                    );
                                }).toList()
                        ).build()
                )
        );
    }
}
