package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.item.Item;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ContextUtils {
    /**
     * 安全获取分页 GUI 对象
     *
     * @param ctx 上下文对象
     * @return 正确泛型类型的 Optional
     */
    public static Optional<PagedGui<?>> getPagedGui(@NotNull Context ctx) {
        return ContextUtils.getMember(ctx, "__gui", PagedGui.class);
    }

    public static Optional<ShopOrder> getShopOrder(@NotNull Context ctx) {
        return ContextUtils.getMember(ctx, "__order", ShopOrder.class);
    }

    /**
     * 安全获取滚动 GUI 对象
     *
     * @param ctx 上下文对象
     * @return 正确泛型类型的 Optional
     */
    public static Optional<ScrollGui<?>> getScrollGui(@NotNull Context ctx) {
        return ContextUtils.getMember(ctx, "__gui", ScrollGui.class);
    }

    /**
     * 统一获取 GUI 对象
     *
     * @param ctx 上下文对象
     * @return 包装为 Optional 的 GUI 基类
     */
    public static Optional<Gui> getGui(@NotNull Context ctx) {
        Optional<PagedGui<?>> paged = getPagedGui(ctx);
        if (paged.isPresent()) {
            return paged.map(g -> g);
        }
        return getScrollGui(ctx).map(g -> g);
    }

    /**
     * 安全获取商店对象
     *
     * @param ctx 上下文对象
     * @return 包装为 Optional 的 Shop 对象
     */
    public static Optional<Shop> getShop(@NotNull Context ctx) {
        return ContextUtils.getMember(ctx, "__shop", Shop.class);
    }

    /**
     * 安全获取商品对象
     *
     * @param ctx 上下文对象
     * @return 包装为 Optional 的 Product 对象
     */
    public static Optional<Product> getProduct(@NotNull Context ctx) {
        return ContextUtils.getMember(ctx, "__product", Product.class);
    }

    public static Optional<Item> getIcon(@NotNull Context ctx) {
        return ContextUtils.getMember(ctx, "__icon", Item.class);
    }

    /**
     * 改进后的泛型安全获取方法（带通配符处理）
     *
     * @param ctx        上下文对象
     * @param memberName 成员名称
     * @param type       目标类型（带通配符）
     * @return 正确泛型类型的 Optional
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getMember(@NotNull Context ctx, @NotNull String memberName, @NotNull Class<?> type) {
        try {
            return Optional.of(ctx.findMember(memberName))
                    .map(Reference::getReferredValue)
                    .map(Value::getValue)
                    .filter(type::isInstance)
                    .map(v -> (T) v);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 安全获取整型参数值
     *
     * @param ctx       上下文对象
     * @param paramName 参数名称
     * @return 包装为 OptionalInt 的整数值
     */
    public static @NotNull OptionalInt getIntParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return OptionalInt.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBigDecimal()
                            .intValue()
            );
        } catch (Exception e) {
            return OptionalInt.empty();
        }
    }

    /**
     * 安全获取整型参数值
     *
     * @param ctx       上下文对象
     * @param paramName 参数名称
     * @return 包装为 OptionalInt 的整数值
     */
    public static @NotNull Optional<Boolean> getBooleanParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBoolean()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull OptionalDouble getDoubleParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return OptionalDouble.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBigDecimal()
                            .doubleValue()
            );
        } catch (Exception e) {
            return OptionalDouble.empty();
        }
    }

    public static @NotNull Optional<Float> getFloatParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBigDecimal()
                            .floatValue()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<String> getStringParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsString()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<Character> getCharacterParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsChar()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<ScriptObject> getScriptObjectParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsScriptObject()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 安全获取玩家对象
     *
     * @param ctx 上下文对象
     * @return 包装为 Optional 的 Player 对象
     */
    public static @NotNull Optional<Player> getPlayer(@NotNull Context ctx) {
        return getMember(ctx, "__player", Player.class);
    }
}