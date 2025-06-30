package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.profile.Profile;
import cn.encmys.ykdz.forest.hyphashop.api.profile.cart.Cart;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.item.builder.CartProductIconBuilder;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CartGUI extends NormalGUI {
    protected final static @NotNull Map<String, Window> windows = new HashMap<>();
    protected final @NotNull Map<String, Gui> guis = new HashMap<>();

    private final @NotNull OfflinePlayer owner;

    public CartGUI(@NotNull OfflinePlayer owner, @NotNull ConfigAccessor config) {
        super(config);
        this.owner = owner;
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
                .map(productLoc -> CartProductIconBuilder.build(cart, productLoc))
                .forEach(contents::add);

        return contents;
    }

    @Override
    public @NotNull Object[] getArgs(@NotNull Player player) {
        return new Object[]{owner};
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
    public @Nullable Gui getGUI(@NotNull Player player) {
        return guis.get(player.getUniqueId().toString());
    }

    @Override
    public @NotNull Consumer<InventoryCloseEvent.Reason> getCloseHandler(@NotNull Player player) {
        return (reason) -> windows.remove(player.getUniqueId().toString());
    }
}
