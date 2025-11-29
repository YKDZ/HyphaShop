package cn.encmys.ykdz.forest.hyphashop.script.object;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.ShopOrder;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.SettlementResultType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.SettlementResult;
import cn.encmys.ykdz.forest.hyphashop.config.MessageConfig;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.shop.order.ShopOrderImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.*;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ScriptObjectAccessor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.AnvilWindow;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@ObjectName("HyphaShopAction")
public class HyphaShopActionObject extends InternalObject {
    @Static
    @Function("add_to_cart")
    @FunctionParas({"amount", "__player", "__shop", "__product"})
    public static void addToCart(@NotNull Context ctx) {
        final Player player = ContextUtils.getPlayer(ctx).orElse(null);
        final Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        final Product product = ShopContextUtils.getProduct(ctx).orElse(null);
        final int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (player == null || shop == null || product == null)
            return;

        if (amount <= 0) {
            LogUtils.warn("Amount of add_to_cart must be greater than 0.");
            return;
        }

        final Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        final ShopOrder cartOrder = profile.getCart().getOrder();
        // 构建一个新订单并等待被检查与合并
        // 避免反复检测购物车中的商品
        final ShopOrder newOrder = new ShopOrderImpl(player)
                .setType(cartOrder.getType())
                .setStack(new ProductLocation(shop.getId(), product.getId()), amount);
        // 一个订单在能被判断成功前必须被计算订单价值
        newOrder.bill();
        // 在一个限制或情况“可以被玩家解决”的情况下
        // 才允许玩家将商品加入购物车
        final SettlementResultType result = newOrder.getType() == OrderType.SELL_TO ? newOrder.canSellTo()
                : newOrder.canBuyFrom();
        if (result.canBeHandleByPlayer())
            cartOrder.combineOrder(newOrder);


        MessageUtils.sendMessage(player,
                MessageConfig.getActionMessage("add-to-cart." + result.getConfigKey(), player.locale()),
                new HashMap<>(), shop, product, player);
    }

    @Static
    @Function("remove_from_cart")
    @FunctionParas({"amount", "__player", "__shop", "__product"})
    public static void removeFromCart(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        Product product = ShopContextUtils.getProduct(ctx).orElse(null);
        int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (player == null || shop == null || product == null)
            return;

        if (amount <= 0) {
            LogUtils.warn("Amount of remove_from_cart must be greater than 0.");
            return;
        }

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        ShopOrder cartOrder = profile.getCart().getOrder();
        ProductLocation productLoc = new ProductLocation(shop.getId(), product.getId());
        if (cartOrder.getOrderedProducts().containsKey(productLoc)) {
            cartOrder.setStack(productLoc, cartOrder.getOrderedProducts().get(productLoc) - amount);
        }
    }

    @Static
    @Function("remove_all_from_cart")
    @FunctionParas({"__player", "__shop", "__product"})
    public static void removeAllFromCart(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        Product product = ShopContextUtils.getProduct(ctx).orElse(null);

        if (player == null || shop == null || product == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        ShopOrder cartOrder = profile.getCart().getOrder();
        ProductLocation productLoc = new ProductLocation(shop.getId(), product.getId());
        if (cartOrder.getOrderedProducts().containsKey(productLoc)) {
            cartOrder.setStack(productLoc, 0);
        }
    }

    @Static
    @Function("sell_to")
    @FunctionParas({"amount", "__player", "__shop", "__product"})
    public static @Nullable String sellToDirectly(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        Product product = ShopContextUtils.getProduct(ctx).orElse(null);
        int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (player == null || shop == null || product == null) {
            LogUtils.warn("Player: " + player + " | Shop: " + shop + " | Product: " + product + " | Amount: " + amount);
            return null;
        }

        if (amount <= 0) {
            return null;
        }

        ShopOrder order = new ShopOrderImpl(player)
                .setType(OrderType.SELL_TO)
                .setStack(new ProductLocation(shop.getId(), product.getId()), amount);

        SettlementResult result = order.settle();

        MessageUtils.sendMessageWithPrefix(player, MessageConfig.getSettleResultMessage(ShoppingMode.DIRECT,
                OrderType.SELL_TO, result.type(), player.locale()), Map.of("cost", result.price()), shop, player, product);

        return result.toString();
    }

    @Static
    @Function("buy_from")
    @FunctionParas({"amount", "__player", "__shop", "__product"})
    public static @Nullable String buyFromDirectly(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        Product product = ShopContextUtils.getProduct(ctx).orElse(null);
        int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (player == null || shop == null || product == null)
            return null;

        if (amount <= 0) {
            LogUtils.warn("Amount of buy_from must be greater than 0.");
            return null;
        }

        ShopOrder order = new ShopOrderImpl(player)
                .setType(OrderType.BUY_FROM)
                .setStack(new ProductLocation(shop.getId(), product.getId()), amount);

        SettlementResult result = order.settle();

        MessageUtils.sendMessageWithPrefix(player, MessageConfig.getSettleResultMessage(ShoppingMode.DIRECT,
                OrderType.BUY_FROM, result.type(), player.locale()), new HashMap<>() {
            {
                put("earned", result.price());
            }
        }, shop, player, product);

        return result.toString();
    }

    @Static
    @Function("buy_all_from")
    @FunctionParas({"__player", "__shop", "__product"})
    public static void buyAllFromDirectly(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        Product product = ShopContextUtils.getProduct(ctx).orElse(null);

        if (player == null || shop == null || product == null)
            return;

        ProductLocation productLoc = new ProductLocation(shop.getId(), product.getId());
        ShopOrder order = new ShopOrderImpl(player)
                .setType(OrderType.BUY_ALL_FROM)
                .modifyStack(productLoc, 1);

        SettlementResult result = order.settle();

        MessageUtils.sendMessageWithPrefix(player, MessageConfig.getSettleResultMessage(ShoppingMode.DIRECT,
                OrderType.BUY_ALL_FROM, result.type(), player.locale()), new HashMap<>() {
            {
                put("earned", result.price());
                put("stack", order.getOrderedProducts().get(productLoc));
            }
        }, shop, player, product);
    }

    @Static
    @Function("scroll")
    @FunctionParas({"amount", "__gui"})
    public static void scroll(@NotNull Context ctx) {
        ScrollGui<?> gui = ShopContextUtils.getScrollGui(ctx).orElse(null);
        int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (gui == null)
            return;

        gui.setLine(gui.getLine() + amount);
    }

    @Static
    @Function("change_page")
    @FunctionParas({"amount", "__gui"})
    public static void changePage(@NotNull Context ctx) {
        PagedGui<?> gui = ShopContextUtils.getPagedGui(ctx).orElse(null);
        int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (gui == null)
            return;

        gui.setPage(gui.getPage() + amount);
    }

    @Static
    @Function("open_shop")
    @FunctionParas({"shop", "__player"})
    public static void openShop(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        String shopId = ContextUtils.getStringParam(ctx, "shop").orElse(null);

        if (shopId == null)
            return;

        Shop shop = HyphaShop.SHOP_FACTORY.getShop(shopId);

        if (shop == null || player == null)
            return;

        shop.getShopGUI().open(player);
    }

    @Static
    @Function("open_gui")
    @FunctionParas({"id", "__player"})
    public static void openGUI(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        String id = ContextUtils.getStringParam(ctx, "id").orElse(null);

        if (id == null || player == null || !HyphaShop.NORMAL_GUI_FACTORY.hasGUI(id))
            return;

        HyphaShop.NORMAL_GUI_FACTORY.getGUI(id).open(player);
    }

    @Static
    @Function("close_gui")
    @FunctionParas({"__player"})
    public static void closeGUI(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null)
            return;

        HyphaShop.PROFILE_FACTORY.getProfile(player).getViewingWindow()
                .ifPresent(window -> Scheduler.runTask((task) -> window.close()));
    }

    @Static
    @Function("switch_shopping_mode")
    @FunctionParas({"__player", "__shop"})
    public static void switchShoppingMode(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);

        if (player == null || shop == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        profile.setShoppingMode(shop.getId(),
                profile.getShoppingMode(shop.getId()) == ShoppingMode.DIRECT ? ShoppingMode.CART : ShoppingMode.DIRECT);
    }

    @Static
    @Function("open_cart")
    @FunctionParas({"__player"})
    public static void openCart(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        profile.getCartGUI().open(player);
    }

    @Static
    @Function("switch_cart_mode")
    @FunctionParas({"__player"})
    public static void switchCartMode(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        ShopOrder cartOrder = profile.getCart().getOrder();
        cartOrder.setType(
                switch (cartOrder.getType()) {
                    case SELL_TO -> OrderType.BUY_FROM;
                    case BUY_FROM -> OrderType.BUY_ALL_FROM;
                    case BUY_ALL_FROM -> OrderType.SELL_TO;
                });

        profile.getCartGUI().updateContents(player);
    }

    @Static
    @Function("clean_cart")
    @FunctionParas({"__player"})
    public static void cleanCart(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        profile.getCart().getOrder().clean();
        profile.getCartGUI().updateContents(player);
    }

    @Static
    @Function("clear_cart")
    @FunctionParas({"__player"})
    public static void clearCart(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        profile.getCart().getOrder().clear();
        profile.getCartGUI().updateContents(player);
    }

    @Static
    @Function("open_order_history")
    @FunctionParas({"__player"})
    public static void openOrderHistory(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        profile.getOrderHistoryGUI().open(player);
    }

    @Static
    @Function("modify_order_stack")
    @FunctionParas({"amount", "__order", "__player", "__product", "__shop"})
    public static void modifyOrderStack(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        ShopOrder order = ShopContextUtils.getShopOrder(ctx).orElse(null);
        Product product = ShopContextUtils.getProduct(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (player == null || order == null || product == null || shop == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        order.modifyStack(new ProductLocation(shop.getId(), product.getId()), amount);
        profile.getCartGUI().updateContents(player);
    }

    @Static
    @Function("set_order_stack")
    @FunctionParas({"amount", "__order", "__player", "__product", "__shop"})
    public static void setOrderStack(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        ShopOrder order = ShopContextUtils.getShopOrder(ctx).orElse(null);
        Product product = ShopContextUtils.getProduct(ctx).orElse(null);
        Shop shop = ShopContextUtils.getShop(ctx).orElse(null);
        int amount = ContextUtils.getIntParam(ctx, "amount").orElse(0);

        if (player == null || order == null || product == null || shop == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        order.setStack(new ProductLocation(shop.getId(), product.getId()), amount);
        profile.getCartGUI().updateContents(player);
    }

    @Static
    @Function("anvil_input")
    @FunctionParas({"gui_config", "__player"})
    public static ScriptObject anvilInput(@NotNull Context ctx) {
        ScriptObject wrapper = InternalObjectManager.FUTURE.newInstance();
        CompletableFuture<Reference> future = new CompletableFuture<>();
        wrapper.declareMember("future", new Reference(new Value(future)));

        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        if (player == null) {
            future.complete(new Reference(new Value("")));
            return wrapper;
        }

        ScriptObject guiConfig = ContextUtils.getScriptObjectParam(ctx, "gui_config").orElse(new ScriptObject());

        AtomicReference<String> result = new AtomicReference<>("");
        Window window = AnvilWindow.builder()
                .setUpperGui(ConfigUtils.parseGui(new ScriptObjectAccessor(guiConfig)))
                .addRenameHandler(result::set)
                .addCloseHandler((reason) -> future.completeAsync(() -> new Reference(new Value(result.get()))))
                .build(player);

        Scheduler.runTask((task) -> window.open());

        return wrapper;
    }

    @Static
    @Function("show_dialog")
    @FunctionParas({"dialog_config", "__player"})
    public static ScriptObject dialogInput(@NotNull Context ctx) {
        ScriptObject wrapper = InternalObjectManager.FUTURE.newInstance();
        CompletableFuture<Reference> future = new CompletableFuture<>();
        wrapper.declareMember("future", new Reference(new Value(future)));

        Player player = ContextUtils.getPlayer(ctx).orElse(null);
        if (player == null) {
            future.complete(new Reference(new Value("")));
            return wrapper;
        }

        ScriptObject dialogConfig = ContextUtils.getScriptObjectParam(ctx, "dialog_config").orElse(new ScriptObject());

        player.showDialog(ConfigUtils.parseDialog(new ScriptObjectAccessor(dialogConfig)));

        return wrapper;
    }

    @Static
    @Function("settle_cart")
    @FunctionParas({"__player"})
    public static void settleCart(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null)
            return;

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(player);
        SettlementResult result = profile.getCart().getOrder().settle();

        MessageUtils.handleSettleCartMessage(player, result);
    }

    @Static
    @Function("update_icon")
    @FunctionParas({"target", "__icon", "__gui_structure", "__gui"})
    public static void updateIcon(@NotNull Context ctx) {
        char target = ContextUtils.getCharacterParam(ctx, "target").orElse('\0');
        Item icon = ShopContextUtils.getIcon(ctx).orElse(null);
        Reference[] structureRef = (Reference[]) ContextUtils.getMember(ctx, "__gui_structure", Reference[].class)
                .orElse(new Reference[0]);
        Gui gui = ShopContextUtils.getGui(ctx).orElse(null);

        if (icon == null)
            return;

        if (target == '\0')
            icon.notifyWindows();
        else {
            if (gui == null)
                return;
            MiscUtils.generatePositionStream(structureRef, target)
                    .forEach(entry -> {
                        Item item = gui.getItem(entry.getKey() * 9 + entry.getValue());
                        if (item == null)
                            return;
                        item.notifyWindows();
                    });
        }
    }

    @Static
    @Function("back")
    @FunctionParas({"__player"})
    public static void back(@NotNull Context ctx) {
        ContextUtils.getPlayer(ctx).ifPresent(player -> HyphaShop.PROFILE_FACTORY.getProfile(player).getPreviousGUI()
                .ifPresent(previousGUI -> previousGUI.open(player)));
    }

    @Static
    @Function("close_dialog")
    @FunctionParas({"__player"})
    public static void closeDialog(@NotNull Context ctx) {
        ContextUtils.getPlayer(ctx).ifPresent(Audience::closeDialog);
    }

    @Static
    @Function("close_inventory")
    @FunctionParas({"__player"})
    public static void closeInventory(@NotNull Context ctx) {
        ContextUtils.getPlayer(ctx).ifPresent(Player::closeInventory);
    }
}
