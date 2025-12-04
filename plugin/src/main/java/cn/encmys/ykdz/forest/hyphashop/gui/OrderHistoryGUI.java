package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.gui.enums.GUIType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.item.builder.NormalIconBuilder;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.*;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OrderHistoryGUI extends NormalGUI {
    protected final static @NotNull Map<String, Window> windows = new HashMap<>();
    protected final static @NotNull Map<String, Gui> guis = new HashMap<>();

    private final @NotNull OfflinePlayer owner;
    private final @NotNull List<Item> contents = new ArrayList<>();
    private final @NotNull BaseItemDecorator icon;
    private final @NotNull BaseItemDecorator placeholderIcon;
    private final @NotNull Script contentLineFormat;
    private final int pageSize;
    private int currentPage = -1;

    public OrderHistoryGUI(@NotNull OfflinePlayer owner, @NotNull ConfigAccessor config) {
        super(config);
        this.owner = owner;
        this.pageSize = 54;

        this.icon = ConfigUtils.parseDecoratorOrDirt(config.getConfig("history-icon")
                        .flatMap((c) -> c.getConfig("icon"))
                        .orElse(null))
                .orElseThrow(() -> new RuntimeException("""
                        Can not parse history-icon.icon from order-history.yml. This should be an issue. Please report it.
                        """));
        this.placeholderIcon = ConfigUtils.parseDecorator(config.getConfig("history-placeholder-icon")
                        .orElse(null))
                .orElseThrow(() -> new RuntimeException("""
                        Can not parse any base item from "history-placeholder-icon.icon" in order-history.yml gui config.
                        """));
        this.contentLineFormat = StringUtils.wrapToScriptWithOmit(config.getConfig("history-icon")
                        .flatMap((c) -> c.getConfig("format"))
                        .flatMap((c) -> c.getString("order-content-line"))
                        .orElse(null))
                .orElseThrow(() -> new RuntimeException("""
                        Error when wrap script from "history-icon.format.order-content-line" in order-history.yml gui config.
                        """));
    }

    @Override
    public void closeAll() {
        Scheduler.runTask((task) -> {
            windows.forEach((uuid, window) -> window.close());
            windows.clear();
        });
    }

    @Override
    public @NotNull Optional<Gui> getGUI(@NotNull Player player) {
        return Optional.ofNullable(guis.get(player.getUniqueId().toString()));
    }

    @Override
    public @NotNull List<Item> getContents(@NotNull Player player) {
        if (contents.isEmpty()) {
            loadMore(player, 1);
        }
        return contents;
    }

    @Override
    public @NotNull BiConsumer<@NotNull Window, @NotNull Player> getOpenedWindowHandler() {
        return (window, player) -> windows.put(player.getUniqueId().toString(), window);
    }

    @Override
    public @NotNull BiConsumer<@NotNull Gui, @NotNull Player> getBuiltGuiHandler() {
        return (gui, player) -> guis.put(player.getUniqueId().toString(), gui);
    }

    @Override
    public @NotNull Consumer<InventoryCloseEvent.Reason> getCloseHandler(@NotNull Player player) {
        return (reason) -> windows.remove(player.getUniqueId().toString());
    }

    @Override
    public @NotNull BiConsumer<@NotNull Integer, @NotNull Integer> getPageChangeHandler(@NotNull Player player) {
        return (from, to) -> {
            if (to < 0) return;
            final int pagesToLoad = (to + 1) - currentPage;
            if (pagesToLoad <= 0) return;

            loadMore(player, pagesToLoad);
            updateContents(player);
        };
    }

    @Override
    public @NotNull Consumer<Player> onBeforeWindowBuild() {
        return (player) -> {
            contents.clear();
            currentPage = -1;
        };
    }

    public @NotNull OfflinePlayer getOwner() {
        return owner;
    }

    @Override
    public @NotNull Object[] getArgs(@NotNull Player player) {
        return new Object[]{owner};
    }

    private void loadMore(@NotNull Player player, int pageAmount) {
        currentPage += pageAmount;

        final int offset = currentPage * pageSize;
        final int limit = pageSize * pageAmount;

        final List<SettlementLog> logs = HyphaShop.DATABASE_FACTORY.getSettlementLogDao()
                .queryLogs(
                        owner.getUniqueId(),
                        offset,
                        limit,
                        OrderType.SELL_TO, OrderType.BUY_FROM, OrderType.BUY_ALL_FROM
                );

        contents.addAll(logs.stream()
                .map(log -> buildIcon(log, player))
                .toList());
    }

    private @NotNull Item buildIcon(@NotNull SettlementLog log, @NotNull Player player) {
        return Item.builder()
                .async(ItemProvider.EMPTY, () -> {
                    // 构造内部列表变量
                    final Map<String, Object> vars = MapUtils.buildImmutableMap(
                            (map) -> {
                                final List<Object> orderContentsLines = new ArrayList<>();
                                for (Map.Entry<ProductLocation, AmountPair> entry : log.getOrderedProducts().entrySet()) {
                                    final ProductLocation productLoc = entry.getKey();
                                    final Shop shop = productLoc.shop().orElse(null);

                                    if (shop == null) continue;

                                    final AmountPair amountPair = entry.getValue();
                                    final Product product = productLoc.product();
                                    final Context parent;
                                    if (product == null) {
                                        parent = ContextUtils.linkContext(
                                                shop.getScriptContext()
                                        );
                                    } else {
                                        parent = ContextUtils.linkContext(
                                                product.getScriptContext(),
                                                shop.getScriptContext()
                                        );
                                    }
                                    orderContentsLines.add(ScriptUtils.evaluateComponent(new VarInjector()
                                                    .withRequiredVars(contentLineFormat)
                                                    .withExtraVars(new HashMap<>() {{
                                                        put("stack", amountPair.stack());
                                                        put("amount", amountPair.amount());
                                                    }})
                                                    .withTarget(new Context(parent))
                                                    .withArg(product)
                                                    .inject()
                                            , contentLineFormat));
                                }
                                map.put("order_contents", orderContentsLines.toArray());
                            }
                    );

                    // 构建显示物品

                    final ItemStack displayItem = log.getOrderedProducts().keySet().stream()
                            // 其他插件的物品不保证在异步环境下能被构建
                            // 例如 MMOItems
                            .filter(productLoc -> {
                                final Product product = productLoc.product();
                                if (product == null) return false;
                                return product.getIconDecorator().getBaseItem().getItemType().isAsyncBuildable();
                            })
                            .findFirst()
                            .map(productLoc -> {
                                final Shop shop = productLoc.shop().orElse(null);
                                final Product product = productLoc.product();
                                if (product == null || shop == null) return new ItemStack(Material.AIR);
                                return NormalIconBuilder.build(product.getIconDecorator(), GUIType.NORMAL, product.getScriptContext(), player, shop, product).getItemProvider(player).get();
                            }).orElse(null);

                    if (displayItem == null || displayItem.getType() == Material.AIR) {
                        return NormalIconBuilder.build(placeholderIcon, GUIType.NORMAL, InternalObjectManager.GLOBAL_OBJECT, player, log)
                                .getItemProvider(player);
                    }

                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(icon, InternalObjectManager.GLOBAL_OBJECT, vars, log, player);

                    return new ItemBuilder(new cn.encmys.ykdz.forest.hyphashop.utils.ItemBuilder(displayItem)
                            .setDisplayName(TextUtils.parseNameToComponent(iconDecorator.getNameOrUseBaseItemName(), InternalObjectManager.GLOBAL_OBJECT, vars, log, player))
                            .setLore(TextUtils.parseLoreToComponent(iconDecorator.getProperty(ItemProperty.LORE), InternalObjectManager.GLOBAL_OBJECT, vars, log, player))
                            .setItemFlags(iconDecorator.getProperty(ItemProperty.ITEM_FLAGS))
                            .build(log.getOrderedProducts().size())
                    );
                })
                .addClickHandler((item, click) -> {
                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(icon, InternalObjectManager.GLOBAL_OBJECT, Map.of(), item, click, click.player());
                    final ActionsConfig actions = iconDecorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, InternalObjectManager.GLOBAL_OBJECT, Collections.emptyMap(), item, click, click.player());
                })
                .build();
    }
}
