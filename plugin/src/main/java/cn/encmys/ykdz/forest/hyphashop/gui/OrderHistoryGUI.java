package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.item.builder.OrderHistoryIconBuilder;
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

public class OrderHistoryGUI extends NormalGUI {
    protected final static @NotNull Map<String, Window> windows = new HashMap<>();
    protected final static @NotNull Map<String, Gui> guis = new HashMap<>();

    private final @NotNull OfflinePlayer owner;
    private final @NotNull List<Item> contents = new ArrayList<>();
    private final int pageSize;
    private int currentPage = -1;

    public OrderHistoryGUI(@NotNull OfflinePlayer owner, @NotNull ConfigAccessor config) {
        super(config);
        this.owner = owner;
        this.pageSize = 54;
    }

    @Override
    public void closeAll() {
        Scheduler.runTask((task) -> {
            windows.forEach((uuid, window) -> window.close());
            windows.clear();
        });
    }

    @Override
    public @Nullable Gui getGUI(@NotNull Player player) {
        return guis.get(player.getUniqueId().toString());
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
            int pagesToLoad = (to + 1) - currentPage;
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

        int offset = currentPage * pageSize;
        int limit = pageSize * pageAmount;

        List<SettlementLog> logs = HyphaShop.DATABASE_FACTORY.getSettlementLogDao()
                .queryLogs(
                        owner.getUniqueId(),
                        offset,
                        limit,
                        OrderType.SELL_TO, OrderType.BUY_FROM, OrderType.BUY_ALL_FROM
                );

        contents.addAll(logs.stream()
                .map(log -> OrderHistoryIconBuilder.build(log, player))
                .toList());
    }
}
