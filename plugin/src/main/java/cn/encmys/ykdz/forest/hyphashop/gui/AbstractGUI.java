package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphashop.HyphaShopImpl;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.enums.ActionEvent;
import cn.encmys.ykdz.forest.hyphashop.api.gui.GUI;
import cn.encmys.ykdz.forest.hyphashop.api.gui.enums.GUIType;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.item.builder.NormalIconBuilder;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.*;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractGUI implements GUI {
    protected static final char markerIdentifier = 'x';

    private @NotNull IngredientPreset buildIconPreset(@NotNull Player player) {
        final IngredientPreset.Builder builder = IngredientPreset.builder();
        Stream.ofNullable(getIconConfig())
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .forEach(entry ->
                        builder.addIngredient(
                                entry.getKey(),
                                NormalIconBuilder.build(entry.getValue(), getType(), getParent(), getArgs(player))
                        )
                );
        return builder.build();
    }

    private @NotNull Gui buildGUI(@NotNull Player player) {
        final Gui gui = switch (getType()) {
            case PAGE -> buildPagedGUI(player);
            case SCROLL -> buildScrollGUI(player);
            case NORMAL -> buildNormalGUI(player);
        };
        getBuiltGuiHandler().accept(gui, player);
        return gui;
    }

    private @NotNull Gui buildScrollGUI(@NotNull Player player) {
        final ScrollGui.Builder<@NotNull Item> guiBuilder = ScrollGui.itemsBuilder()
                .setStructure(getStructure().toArray(new String[0]))
                .addScrollHandler(getScrollHandler())
                .addIngredient(markerIdentifier, getScrollMode().orElse(Markers.CONTENT_LIST_SLOT_HORIZONTAL))
                .setContent(getContents(player))
                .applyPreset(buildIconPreset(player));

        return guiBuilder.build();
    }

    private @NotNull Gui buildPagedGUI(@NotNull Player player) {
        final PagedGui.Builder<@NotNull Item> guiBuilder = PagedGui.itemsBuilder()
                .setStructure(getStructure().toArray(new String[0]))
                .addPageChangeHandler(getPageChangeHandler(player))
                .addIngredient(markerIdentifier, getPageMode().orElse(Markers.CONTENT_LIST_SLOT_HORIZONTAL))
                .setContent(getContents(player))
                .applyPreset(buildIconPreset(player));

        return guiBuilder.build();
    }

    private @NotNull Gui buildNormalGUI(@NotNull Player player) {
        final TabGui.Builder guiBuilder = TabGui.builder()
                .setStructure(getStructure().toArray(new String[0]))
                .applyPreset(buildIconPreset(player));

        return guiBuilder.build();
    }

    public final void open(@NotNull Player player) {
        onBeforeWindowBuild().accept(player);

        final Window window = Window.builder()
                .setUpperGui(buildGUI(player))
                .setTitleSupplier(getTitleSupplier(player))
                .addOpenHandler(() -> MiscUtils.processActions(ActionEvent.GUI_ON_OPEN, getActions(), getParent(), Collections.emptyMap(), getArgs(player)))
                .addCloseHandler(getCloseHandler(player))
                .addCloseHandler((reason) -> MiscUtils.processActions(ActionEvent.GUI_ON_CLOSE, getActions(), getParent(), Collections.emptyMap(), getArgs(player)))
                .addCloseHandler((reason) -> HyphaShopImpl.PROFILE_FACTORY.getProfile(player).setViewingWindow(null))
                .addCloseHandler((reason) -> HyphaShopImpl.PROFILE_FACTORY.getProfile(player).setPreviousGUI(this))
                .addOutsideClickHandler(event -> MiscUtils.processActions(ActionEvent.GUI_ON_OUTSIDE_CLICK, getActions(), getParent(), Collections.emptyMap(), getArgs(player)))
                .build(player);

        Scheduler.runTask((task) -> {
            window.open();

            final long updatePeriod = getTitleUpdatePeriod();
            if (updatePeriod > 0) {
                // GUI 开启的一瞬间也是一次刷新标题
                // 故加上与间隔等长的 delay
                Scheduler.runAsyncTaskAtFixedRate((task2) -> {
                    if (!window.isOpen()) {
                        task2.cancel();
                        return;
                    }
                    window.updateTitle();
                }, updatePeriod, updatePeriod);
            }

            HyphaShopImpl.PROFILE_FACTORY.getProfile(player).setViewingWindow(window);
        });

        getOpenedWindowHandler().accept(window, player);
    }

    public @NotNull GUIType getType() {
        return getPageMode()
                .map(marker -> GUIType.PAGE)
                .or(() -> getScrollMode().map(marker -> GUIType.SCROLL))
                .orElse(GUIType.NORMAL);
    }


    @SuppressWarnings("unchecked")
    public void updateContents(@NotNull Player player) {
        if (getGUI(player).isEmpty()) return;

        final Gui gui = getGUI(player).get();
        if (gui instanceof ScrollGui<?>) {
            ((ScrollGui<@NotNull Item>) gui).setContent(getContents(player));
        } else if (gui instanceof PagedGui<?>) {
            ((PagedGui<@NotNull Item>) gui).setContent(getContents(player));
        }
    }

    public @NotNull Consumer<Player> onBeforeWindowBuild() {
        return player -> {
        };
    }

    public abstract @NotNull Optional<Gui> getGUI(@NotNull Player player);

    public abstract @NotNull Supplier<@NotNull Component> getTitleSupplier(@NotNull Player player);

    public abstract @NotNull Map<Character, BaseItemDecorator> getIconConfig();

    public abstract @NotNull Optional<Marker> getScrollMode();

    public abstract @NotNull Optional<Marker> getPageMode();

    public abstract @NotNull Context getParent();

    public abstract @NotNull Object[] getArgs(@NotNull Player player);

    public abstract @NotNull List<Item> getContents(@NotNull Player player);

    public abstract long getTitleUpdatePeriod();

    public abstract @NotNull Consumer<InventoryCloseEvent.Reason> getCloseHandler(@NotNull Player player);

    public abstract @NotNull BiConsumer<@NotNull Window, @NotNull Player> getOpenedWindowHandler();

    public abstract @NotNull BiConsumer<@NotNull Gui, @NotNull Player> getBuiltGuiHandler();

    public abstract @NotNull Optional<Window> getWindow(@NotNull Player player);

    public abstract @NotNull BiConsumer<@NotNull Integer, @NotNull Integer> getScrollHandler();

    public abstract @NotNull BiConsumer<@NotNull Integer, @NotNull Integer> getPageChangeHandler(@NotNull Player player);
}
