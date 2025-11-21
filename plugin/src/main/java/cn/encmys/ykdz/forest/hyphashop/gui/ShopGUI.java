package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.Shop;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.item.builder.ProductIconBuilder;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ShopGUI extends NormalGUI {
    protected final static @NotNull Map<String, Map<String, Window>> windows = new HashMap<>();
    protected final static @NotNull Map<String, Map<String, Gui>> guis = new HashMap<>();

    private final @NotNull Shop shop;

    public ShopGUI(@NotNull ConfigAccessor config, @NotNull Shop shop) {
        super(config);
        this.shop = shop;
        windows.put(shop.getId(), new HashMap<>());
        guis.put(shop.getId(), new HashMap<>());
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
                .map(product -> ProductIconBuilder.build(shop, product))
                .toList();
    }

    @Override
    public @NotNull Consumer<InventoryCloseEvent.Reason> getCloseHandler(@NotNull Player player) {
        return (reason) -> windows.get(shop.getId()).remove(player.getUniqueId().toString());
    }

    @Override
    public @NotNull BiConsumer<@NotNull Gui, @NotNull Player> getBuiltGuiHandler() {
        return (gui, player) -> guis.get(shop.getId()).put(player.getUniqueId().toString(), gui);
    }

    @Override
    public @Nullable Window getWindow(@NotNull Player player) {
        return windows.get(shop.getId()).get(player.getUniqueId().toString());
    }

    @Override
    public @NotNull Optional<Gui> getGUI(@NotNull Player player) {
        return Optional.ofNullable(guis.get(shop.getId()).get(player.getUniqueId().toString()));
    }

    @Override
    public void updateContentsForAllViewers() {
        windows.get(shop.getId()).values().stream()
                .map(Window::getViewer)
                .forEach(this::updateContents);
    }

    @Override
    public @NotNull BiConsumer<@NotNull Window, @NotNull Player> getOpenedWindowHandler() {
        return (window, player) -> windows.get(shop.getId()).put(player.getUniqueId().toString(), window);
    }

    @Override
    public void closeAll() {
        Scheduler.runTask(task -> {
            // 先收集后遍历
            // 因为 close 会修改 windows 本身导致异常
            new ArrayList<>(windows.getOrDefault(shop.getId(), new HashMap<>()).values()).forEach(Window::close);
        });
    }
}
