package cn.encmys.ykdz.forest.hyphashop.gui;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphashop.api.config.action.ActionsConfig;
import cn.encmys.ykdz.forest.hyphashop.api.item.decorator.BaseItemDecorator;
import cn.encmys.ykdz.forest.hyphashop.api.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphashop.api.utils.config.ConfigAccessor;
import cn.encmys.ykdz.forest.hyphashop.scheduler.Scheduler;
import cn.encmys.ykdz.forest.hyphashop.utils.ConfigUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.ScriptUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.TextUtils;
import cn.encmys.ykdz.forest.hyphashop.utils.config.ConfigurationSectionAccessor;
import cn.encmys.ykdz.forest.hyphashop.var.VarInjector;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Marker;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NormalGUI extends AbstractGUI {
    private static final @NotNull Map<String, Window> windows = new HashMap<>();
    private static final @NotNull Map<String, Gui> guis = new HashMap<>();

    private final @NotNull ConfigAccessor config;

    public NormalGUI(@NotNull ConfigAccessor config) {
        this.config = config;
    }

    @Override
    public @NotNull Optional<Gui> getGUI(@NotNull Player player) {
        return Optional.ofNullable(guis.get(player.getName()));
    }

    @Override
    public @NotNull Supplier<@NotNull Component> getTitleSupplier(@NotNull Player player) {
        if (config.isList("title")) {
            final AtomicInteger index = new AtomicInteger(0);
            return () -> {
                final VarInjector injector = new VarInjector()
                        .withTarget(new Context(getParent()))
                        .withArgs(getArgs(player));
                final List<Script> script = StringUtils.wrapToScriptWithOmit(config.getStringList("title").orElse(null));
                injector.withRequiredVars(script);
                final int currentIndex = index.getAndIncrement() % script.size();
                return HyphaAdventureUtils.getComponentFromMiniMessage(ScriptUtils.evaluateString(injector.inject(), script.get(currentIndex)));
            };
        } else if (config.isString("title")) {
            return () -> {
                final VarInjector injector = new VarInjector()
                        .withTarget(new Context(getParent()))
                        .withArgs(getArgs(player));
                final Script script = StringUtils.wrapToScriptWithOmit(config.getString("title").orElse(null));
                assert script != null;
                injector.withRequiredVars(script);
                return HyphaAdventureUtils.getComponentFromMiniMessage(ScriptUtils.evaluateString(injector.inject(), script));
            };
        }

        return Component::empty;
    }

    @Override
    public @NotNull Map<Character, BaseItemDecorator> getIconConfig() {
        return ConfigUtils.parseIconDecorators(config.getConfig("icons").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));
    }

    @Override
    public @NotNull Optional<Marker> getScrollMode() {
        return config.getString("scroll-mode").map(mode -> mode.equals("HORIZONTAL") ? Markers.CONTENT_LIST_SLOT_HORIZONTAL : Markers.CONTENT_LIST_SLOT_VERTICAL);
    }

    @Override
    public @NotNull Optional<Marker> getPageMode() {
        return config.getString("page-mode").map(mode -> mode.equals("HORIZONTAL") ? Markers.CONTENT_LIST_SLOT_HORIZONTAL : Markers.CONTENT_LIST_SLOT_VERTICAL);
    }

    @Override
    public @NotNull Context getParent() {
        return Context.GLOBAL_OBJECT;
    }

    @Override
    public @NotNull Object[] getArgs(@NotNull Player player) {
        return new Object[]{player};
    }

    @Override
    public @NotNull List<String> getStructure() {
        return config.getStringList("structure").orElse(new ArrayList<>());
    }

    @Override
    public @NotNull ActionsConfig getActions() {
        return ActionsConfig.of(config.getConfig("actions").orElse(new ConfigurationSectionAccessor(new YamlConfiguration())));
    }

    @Override
    public @NotNull List<Item> getContents(@NotNull Player player) {
        return List.of();
    }

    @Override
    public long getTitleUpdatePeriod() {
        return TextUtils.parseTimeStringToTicks(config.getString("title-update-period").orElse("0s"));
    }

    @Override
    public @NotNull Consumer<InventoryCloseEvent.Reason> getCloseHandler(@NotNull Player player) {
        return (reason) -> windows.remove(player.getUniqueId().toString());
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
    public void closeAll() {
        Scheduler.runTask((task) -> windows.values().forEach(Window::close));
    }

    @Override
    public void updateContentsForAllViewers() {
    }

    @Override
    public @Nullable Window getWindow(@NotNull Player player) {
        return windows.get(player.getUniqueId().toString());
    }

    @Override
    public @NotNull BiConsumer<@NotNull Integer, @NotNull Integer> getScrollHandler() {
        return (from, to) -> {
        };
    }

    @Override
    public @NotNull BiConsumer<@NotNull Integer, @NotNull Integer> getPageChangeHandler(@NotNull Player player) {
        return (from, to) -> {
        };
    }
}
