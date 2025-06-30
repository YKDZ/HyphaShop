package cn.encmys.ykdz.forest.hyphashop.item.builder;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.gui.enums.GUIType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.utils.*;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class NormalIconBuilder {
    public static @NotNull Item build(@NotNull BaseItemDecorator staticDecorator, @NotNull GUIType guiType, @NotNull Context parent, @Nullable Object @NotNull ... args) {
        // 按理讲只有控制图标才需要使用 buildPageIcon 和 buildScrollIcon 构建
        // 但是现在没有更好的办法判定一个图标是否是控制图标
        // 故将有 actions 的图标全部视为控制图标
        if (staticDecorator.getProperty(ItemProperty.ACTIONS) == null)
            return buildNormalIcon(staticDecorator, parent, args);

        return switch (guiType) {
            case PAGE -> buildPageIcon(staticDecorator, parent, args);
            case SCROLL -> buildScrollIcon(staticDecorator, parent, args);
            case NORMAL -> buildNormalIcon(staticDecorator, parent, args);
        };
    }

    private static @NotNull Item buildNormalIcon(@NotNull BaseItemDecorator staticDecorator, @NotNull Context parent, @Nullable Object @NotNull ... args) {
        final var builder = Item.builder()
                .setItemProvider((player) -> {
                    final BaseItemDecorator decorator = DecoratorUtils.selectDecoratorByCondition(
                            staticDecorator, parent, player
                    );
                    return itemFromDecorator(decorator, player, parent, args);
                })
                .addClickHandler((item, click) -> {
                    final Player player = click.player();
                    final BaseItemDecorator decorator = DecoratorUtils.selectDecoratorByCondition(staticDecorator, parent, player, click, item);
                    final ActionsConfig actions = decorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, parent, Collections.emptyMap(), Stream.concat(Arrays.stream(args), Stream.of(player, click, item)).toArray());
                });
        if (Boolean.TRUE.equals(staticDecorator.getProperty(ItemProperty.UPDATE_ON_CLICK))) builder.updateOnClick();
        final Integer period = staticDecorator.getProperty(ItemProperty.UPDATE_PERIOD);
        if (period != null) builder.updatePeriodically(period);
        return builder.build();
    }

    private static @NotNull Item buildScrollIcon(@NotNull BaseItemDecorator staticDecorator, @NotNull Context parent, @Nullable Object @NotNull ... args) {
        final var builder = BoundItem.scrollBuilder()
                .setItemProvider((player, gui) -> {
                    final BaseItemDecorator decorator = DecoratorUtils.selectDecoratorByCondition(staticDecorator, parent, Stream.concat(Arrays.stream(args), Stream.of(player, gui)).toArray());
                    return itemFromDecorator(decorator, player, parent, Stream.concat(Arrays.stream(args), Stream.of(gui)).toArray());
                })
                .addClickHandler((item, gui, click) -> {
                    final Player player = click.player();

                    final BaseItemDecorator decorator = DecoratorUtils.selectDecoratorByCondition(staticDecorator, parent, Stream.concat(Arrays.stream(args), Stream.of(player, click, item, gui)).toArray());
                    final ActionsConfig actions = decorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, parent, Collections.emptyMap(), Stream.concat(Arrays.stream(args), Stream.of(player, click, item, gui)).toArray());
                });
        if (Boolean.TRUE.equals(staticDecorator.getProperty(ItemProperty.UPDATE_ON_CLICK))) builder.updateOnClick();
        final Integer period = staticDecorator.getProperty(ItemProperty.UPDATE_PERIOD);
        if (period != null) builder.updatePeriodically(period);
        return builder.build();
    }

    private static @NotNull Item buildPageIcon(@NotNull BaseItemDecorator staticDecorator, @NotNull Context parent, @Nullable Object @NotNull ... args) {
        final var builder = BoundItem.pagedBuilder()
                .setItemProvider((player, gui) -> {
                    final BaseItemDecorator decorator = DecoratorUtils.selectDecoratorByCondition(staticDecorator, parent, Stream.concat(Arrays.stream(args), Stream.of(player, gui)).toArray());
                    return itemFromDecorator(decorator, player, parent, Stream.concat(Arrays.stream(args), Stream.of(gui)).toArray());
                })
                .addClickHandler((item, gui, click) -> {
                    final Player player = click.player();

                    final BaseItemDecorator decorator = DecoratorUtils.selectDecoratorByCondition(staticDecorator, parent, Stream.concat(Arrays.stream(args), Stream.of(player, click, item, gui)).toArray());
                    final ActionsConfig actions = decorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, parent, Collections.emptyMap(), Stream.concat(Arrays.stream(args), Stream.of(player, click, item, gui)).toArray());
                });
        if (Boolean.TRUE.equals(staticDecorator.getProperty(ItemProperty.UPDATE_ON_CLICK))) builder.updateOnClick();
        final Integer period = staticDecorator.getProperty(ItemProperty.UPDATE_PERIOD);
        if (period != null) builder.updatePeriodically(period);
        return builder.build();
    }

    private static @NotNull xyz.xenondevs.invui.item.ItemBuilder itemFromDecorator(@NotNull BaseItemDecorator decorator, @NotNull Player player, @NotNull Context parent, @Nullable Object @NotNull ... args) {
        final Script amount = decorator.getProperty(ItemProperty.AMOUNT);

        if (amount == null) {
            throw new IllegalArgumentException("No amount specified");
        }

        final List<Object> argList = new ArrayList<>(Arrays.asList(args));
        argList.add(player);

        return new xyz.xenondevs.invui.item.ItemBuilder(
                new ItemBuilder(decorator.getBaseItem().build(player))
                        .setDisplayName(TextUtils.parseNameToComponent(decorator.getNameOrUseBaseItemName(), parent, argList.toArray()))
                        .setLore(TextUtils.parseLoreToComponent(decorator.getProperty(ItemProperty.LORE), parent, argList.toArray()))
                        .setItemFlags(decorator.getProperty(ItemProperty.ITEM_FLAGS))
                        .setBannerPatterns(decorator.getProperty(ItemProperty.BANNER_PATTERNS))
                        .setFireworkEffects(decorator.getProperty(ItemProperty.FIREWORK_EFFECTS))
                        .setEnchantments(decorator.getProperty(ItemProperty.ENCHANTMENTS))
                        .setPotionEffects(decorator.getProperty(ItemProperty.POTION_EFFECTS))
                        .setArmorTrim(decorator.getProperty(ItemProperty.ARMOR_TRIM))
                        .setEnchantable(decorator.getProperty(ItemProperty.ENCHANTABLE))
                        .setEnchantGlint(decorator.getProperty(ItemProperty.ENCHANT_GLINT))
                        .setGlider(decorator.getProperty(ItemProperty.GLIDER))
                        .setFlightDuration(decorator.getProperty(ItemProperty.FLIGHT_DURATION))
                        .setPotionCustomColor(decorator.getProperty(ItemProperty.POTION_COLOR))
                        .setPotionType(decorator.getProperty(ItemProperty.POTION_TYPE))
                        .setPotionCustomName(decorator.getProperty(ItemProperty.POTION_CUSTOM_NAME))
                        .setCustomModelData(decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_FLAGS), decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_COLORS), decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_FLOATS), decorator.getProperty(ItemProperty.CUSTOM_MODEL_DATA_STRINGS))
                        .build(ScriptUtils.evaluateInt(new VarInjector()
                                .withTarget(new Context(parent))
                                .withRequiredVars(amount)
                                .withArgs(args)
                                .withArg(player)
                                .inject(), amount))
        );
    }
}
