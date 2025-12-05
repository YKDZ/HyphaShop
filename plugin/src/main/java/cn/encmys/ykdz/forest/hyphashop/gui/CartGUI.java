package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.profile.cart.Cart;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.*;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CartGUI extends NormalGUI {
    protected final static @NotNull Map<UUID, Window> windows = new HashMap<>();
    protected final @NotNull Map<UUID, Gui> guis = new HashMap<>();

    private final @NotNull BaseItemDecorator productIcon;
    private final @NotNull OfflinePlayer owner;

    public CartGUI(@NotNull OfflinePlayer owner, @NotNull ConfigAccessor config) {
        super(config);
        this.owner = owner;

        this.productIcon = ConfigUtils.parseDecoratorOrDirt(config.getConfig("cart-product-icon")
                        .orElse(null))
                .orElseThrow(() -> new RuntimeException("""
                        Can not parse product-icon decorator from gui/internal/cart.yml. This should be an issue. Please report it.
                        """));
    }

    @Override
    public void closeAll() {
        Scheduler.runTask((task) -> windows.values().forEach(Window::close));
    }

    @Override
    public @NotNull List<Item> getContents(@NotNull Player player) {
        List<Item> contents = new ArrayList<>();

        Profile profile = HyphaShop.PROFILE_FACTORY.getProfile(owner);
        Cart cart = profile.getCart();

        cart.getOrder().getOrderedProducts().keySet().stream()
                .map(productLoc -> build(cart, productLoc))
                .forEach(contents::add);

        return contents;
    }

    @Override
    public @NotNull Object[] getArgs(@NotNull Player player) {
        return new Object[]{owner};
    }

    @Override
    public @NotNull BiConsumer<@NotNull Window, @NotNull Player> getOpenedWindowHandler() {
        return (window, player) -> windows.put(player.getUniqueId(), window);
    }

    @Override
    public @NotNull BiConsumer<@NotNull Gui, @NotNull Player> getBuiltGuiHandler() {
        return (gui, player) -> guis.put(player.getUniqueId(), gui);
    }

    @Override
    public @NotNull Optional<Gui> getGUI(@NotNull Player player) {
        return Optional.ofNullable(guis.get(player.getUniqueId()));
    }

    @Override
    public @NotNull Consumer<InventoryCloseEvent.Reason> getCloseHandler(@NotNull Player player) {
        return (reason) -> windows.remove(player.getUniqueId());
    }

    private @NotNull Item build(@NotNull Cart cart, @NotNull ProductLocation productLoc) {
        final Product product = productLoc.product();
        final Shop shop = productLoc.shop().orElse(null);

        if (product == null || shop == null) return Item.simple(new ItemStack(Material.AIR));

        var builder = Item.builder()
                .setItemProvider((player) -> {
                    final int stack = cart.getOrder().getOrderedProducts().getOrDefault(productLoc, 0);
                    if (stack <= 0) {
                        return new xyz.xenondevs.invui.item.ItemBuilder(Material.AIR);
                    }

                    final Map<String, Object> vars = Map.of(
                            "stack", stack,
                            "total_prices", cart.getOrder().getBilledPrice(productLoc)
                    );

                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(productIcon, ContextUtils.linkContext(
                            product.getScriptContext().clone(),
                            shop.getScriptContext().clone()
                    ), vars, player, shop, product, cart.getOrder());

                    // 在商品自己图标的基础上覆盖名称、lore 和 itemFlags
                    return new xyz.xenondevs.invui.item.ItemBuilder(
                            new ItemBuilder(product.getIconDecorator().getBaseItem().build(player))
                                    .decorate(product.getIconDecorator())
                                    .setDisplayName(TextUtils.parseNameToComponent(iconDecorator.getNameOrUseBaseItemName(), ContextUtils.linkContext(
                                            product.getScriptContext().clone(),
                                            shop.getScriptContext().clone()
                                    ), vars, player, shop, product, cart.getOrder()))
                                    .setLore(TextUtils.parseLoreToComponent(iconDecorator.getProperty(ItemProperty.LORE), ContextUtils.linkContext(
                                            product.getScriptContext().clone(),
                                            shop.getScriptContext().clone()
                                    ), vars, player, shop, product, cart.getOrder()))
                                    .build(stack)
                    );
                })
                .addClickHandler((item, click) -> {
                    final Player player = click.player();

                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(productIcon, ContextUtils.linkContext(
                            product.getScriptContext().clone(),
                            shop.getScriptContext().clone()
                    ), Map.of(), player, shop, product, click, item, cart.getOrder());
                    final ActionsConfig actions = iconDecorator.getProperty(ItemProperty.ACTIONS);

                    HyphaShopImpl.LOGGER.debug("""
                            About to handle click of cart product icon. Click: %s, Player: %s, Actions: %s
                            """.formatted(click, player.getUniqueId(), actions));

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, ContextUtils.linkContext(
                            product.getScriptContext(),
                            shop.getScriptContext()
                    ), Map.of(), player, shop, product, click, item, cart.getOrder());
                });

        if (Boolean.TRUE.equals(productIcon.getProperty(ItemProperty.UPDATE_ON_CLICK))) builder.updateOnClick();
        final Integer period = productIcon.getProperty(ItemProperty.UPDATE_PERIOD);
        if (period != null) builder.updatePeriodically(period);
        return builder.build();
    }
}
