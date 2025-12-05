package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionClickType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.enums.ItemProperty;
import cn.encmys.ykdz.forest.hyphashop.api.product.Product;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.product.BundleProduct;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.*;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ShopGUI extends NormalGUI {
    protected final static @NotNull Map<String, Map<UUID, Window>> windows = new HashMap<>();
    protected final static @NotNull Map<String, Map<UUID, Gui>> guis = new HashMap<>();

    /**
     *
     * 储存用于格式化商品图标的信息的 Decorator<br/>
     * 如 name，lore 和 actions 等都来自它<br/>
     * 且是静态的，用于进行条件判断以获取动态的 iconDecorator<br/>
     */
    private final @NotNull BaseItemDecorator productIcon;
    private final @Nullable Script bundleContentLine;
    private final @NotNull Shop shop;

    public ShopGUI(@NotNull Shop shop, @NotNull ConfigAccessor config) {
        super(config);
        this.shop = shop;

        this.productIcon = ConfigUtils.parseDecoratorOrDirt(config.getConfig("product-icon")
                        .flatMap((c) -> c.getConfig("icon"))
                        .orElse(null))
                .orElseThrow(() -> new RuntimeException("""
                        Can not parse product-icon decorator from shop config %s.yml. This should be an issue. Please report it.
                        """.formatted(shop.getId())));
        this.bundleContentLine = StringUtils.wrapToScriptWithOmit(config.getConfig("product-icon")
                        .flatMap((c) -> c.getConfig("format"))
                        .flatMap((c) -> c.getString("bundle-content-line"))
                        .orElse(null))
                .orElseGet(() -> {
                    HyphaShopImpl.LOGGER.warn("""
                            Shop config %s.yml does not have product-icon.format.bundle-content-line given. No bundle content line will be shown.
                            """.formatted(shop.getId())
                    );
                    return null;
                });
    }

    @Override
    public @NotNull Context getParent() {
        return shop.getScriptContext();
    }

    @Override
    public @NotNull Object[] getArgs(@NotNull Player player) {
        return new Object[]{shop, player};
    }

    @Override
    public @NotNull List<Item> getContents(@NotNull Player player) {
        return shop.getShopStocker().getListedProducts().stream()
                .map(productId -> HyphaShop.PRODUCT_FACTORY.getProduct(productId))
                .filter(Objects::nonNull)
                .map(this::build)
                .toList();
    }

    @Override
    public @NotNull Consumer<InventoryCloseEvent.Reason> getCloseHandler(@NotNull Player player) {
        return (reason) -> windows
                .computeIfAbsent(shop.getId(), id -> new HashMap<>())
                .remove(player.getUniqueId());
    }

    @Override
    public @NotNull BiConsumer<@NotNull Gui, @NotNull Player> getBuiltGuiHandler() {
        return (gui, player) -> guis
                .computeIfAbsent(shop.getId(), id -> new HashMap<>())
                .put(player.getUniqueId(), gui);
    }

    @Override
    public @NotNull Optional<Window> getWindow(@NotNull Player player) {
        return Optional.ofNullable(
                windows.getOrDefault(shop.getId(), Map.of())
                        .get(player.getUniqueId())
        );
    }

    @Override
    public @NotNull Optional<Gui> getGUI(@NotNull Player player) {
        return Optional.ofNullable(
                guis.getOrDefault(shop.getId(), Map.of())
                        .get(player.getUniqueId())
        );
    }

    @Override
    public void updateContentsForAllViewers() {
        Optional.ofNullable(windows.get(shop.getId()))
                .ifPresent(map -> map.values().stream()
                        .map(Window::getViewer)
                        .forEach(this::updateContents)
                );
    }

    @Override
    public @NotNull BiConsumer<@NotNull Window, @NotNull Player> getOpenedWindowHandler() {
        return (window, player) -> windows
                .computeIfAbsent(shop.getId(), id -> new HashMap<>())
                .put(player.getUniqueId(), window);
    }

    @Override
    public void closeAll() {
        Scheduler.runTask(task -> {
            // 先收集后遍历
            // 因为 close 会修改 windows 本身导致异常
            new ArrayList<>(windows.getOrDefault(shop.getId(), new HashMap<>()).values()).forEach(Window::close);
        });
    }

    private @NotNull Item build(@NotNull Product product) {
        return build(product, shop.getShopCounter().getAmount(product.getId()));
    }

    private @NotNull Item build(@NotNull Product product, int amount) {
        // 储存商品图标本身信息的 Decorator（如 base 和 desc_lore）
        // 需要与下文 staticIconDecorator 区分开
        final BaseItemDecorator productIconDecorator = product.getIconDecorator();

        var builder = Item.builder()
                .setItemProvider((player) -> {
                    final Context parent = ContextUtils.linkContext(
                            product.getScriptContext(),
                            shop.getScriptContext()
                    );

                    final List<Component> bundleContentsLore = new ArrayList<>();
                    if (bundleContentLine != null && product instanceof BundleProduct) {
                        final Map<String, Integer> bundleContents = ((BundleProduct) product).getBundleContents();
                        if (!bundleContents.isEmpty()) {
                            for (final Map.Entry<String, Integer> entry : bundleContents.entrySet()) {
                                final Product content = HyphaShop.PRODUCT_FACTORY.getProduct(entry.getKey());
                                final int stack = entry.getValue();
                                if (content == null) {
                                    continue;
                                }
                                final Map<String, Object> vars = Map.of(
                                        "stack", stack,
                                        "total_amount", stack * amount
                                );
                                bundleContentsLore.add(ScriptUtils.evaluateComponent(new VarInjector()
                                        .withArg(content)
                                        .withArg(shop)
                                        .withRequiredVars(bundleContentLine)
                                        .withTarget(new Context(ContextUtils.linkContext(
                                                content.getScriptContext(),
                                                shop.getScriptContext()
                                        )))
                                        .withExtraVars(vars)
                                        .inject(), bundleContentLine));
                            }
                        }
                    }

                    // 额外变量
                    final Map<String, Object> vars = MapUtils.buildImmutableMap((map) -> {
                        {
                            final List<Script> descLore = productIconDecorator.getProperty(ItemProperty.LORE);
                            if (descLore != null) {
                                map.put("desc_lore", descLore.stream()
                                        .map(lore -> ScriptUtils.evaluateComponentList(new VarInjector()
                                                .withTarget(new Context(parent))
                                                .withRequiredVars(descLore)
                                                .withArgs(product, shop)
                                                .inject(), lore))
                                        .flatMap(List::stream)
                                        .toArray(Component[]::new));
                            } else {
                                map.put("desc_lore", new Component[0]);
                            }
                        }
                        map.put("bundle_contents_lore", bundleContentsLore.toArray(new Component[0]));
                    });

                    final BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(productIcon, parent, vars, shop, product, player);

                    return new ItemBuilder(
                            new cn.encmys.ykdz.forest.hyphashop.utils.ItemBuilder(productIconDecorator.getBaseItem().build(player))
                                    // 只有 displayName 和 lore
                                    // 会使用 iconDecorator 的设置
                                    // 其他都会继承 productIconDecorator
                                    .decorate(productIconDecorator)
                                    .setDisplayName(TextUtils.parseNameToComponent(iconDecorator.getNameOrUseBaseItemName(), parent, vars, shop, product, player))
                                    .setLore(TextUtils.parseLoreToComponent(iconDecorator.getProperty(ItemProperty.LORE), parent, vars, shop, product, player))
                                    .build(amount));
                })
                .addClickHandler((item, click) -> {
                    Player player = click.player();

                    Context parent = ContextUtils.linkContext(
                            product.getScriptContext().clone(),
                            shop.getScriptContext().clone()
                    );

                    BaseItemDecorator iconDecorator = DecoratorUtils.selectDecoratorByCondition(productIcon, parent, Map.of(), player, shop, click, product, item);
                    ActionsConfig actions = iconDecorator.getProperty(ItemProperty.ACTIONS);

                    MiscUtils.processActions(ActionClickType.fromClickType(click.clickType()), actions, parent, Collections.emptyMap(), player, shop, click, product, item);
                });

        // 优先尊重 productIconDecorator 的更新设置
        if (Boolean.TRUE.equals(productIconDecorator.getProperty(ItemProperty.UPDATE_ON_CLICK)))
            builder.updateOnClick();
        else if (Boolean.TRUE.equals(productIcon.getProperty(ItemProperty.UPDATE_ON_CLICK)))
            builder.updateOnClick();

        Integer period = productIconDecorator.getProperty(ItemProperty.UPDATE_PERIOD) != null ?
                productIconDecorator.getProperty(ItemProperty.UPDATE_PERIOD)
                : productIcon.getProperty(ItemProperty.UPDATE_PERIOD);
        if (period != null) builder.updatePeriodically(period);

        return builder.build();
    }
}
