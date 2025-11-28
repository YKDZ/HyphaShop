package cn.encmys.ykdz.forest.hyphashop.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.item.Item;

import java.util.Optional;

public class ShopContextUtils {
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
}