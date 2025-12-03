package cn.encmys.ykdz.forest.hyphashop;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.ScriptManager;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.config.*;
import cn.encmys.ykdz.forest.hyphashop.currency.manager.CurrencyManagerImpl;
import cn.encmys.ykdz.forest.hyphashop.database.factory.DatabaseFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.gui.factory.NormalGUIFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.hook.ItemsAdderHook;
import cn.encmys.ykdz.forest.hyphashop.hook.MMOItemsHook;
import cn.encmys.ykdz.forest.hyphashop.hook.MythicMobsHook;
import cn.encmys.ykdz.forest.hyphashop.hook.PlaceholderAPIHook;
import cn.encmys.ykdz.forest.hyphashop.listener.ItemsAdderListener;
import cn.encmys.ykdz.forest.hyphashop.listener.PlayerListener;
import cn.encmys.ykdz.forest.hyphashop.logger.Logger;
import cn.encmys.ykdz.forest.hyphashop.product.factory.ProductFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.profile.factory.ProfileFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.rarity.factory.RarityFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.scheduler.ConnTasksImpl;
import cn.encmys.ykdz.forest.hyphashop.script.object.HyphaShopActionObject;
import cn.encmys.ykdz.forest.hyphashop.script.object.HyphaShopBasicObject;
import cn.encmys.ykdz.forest.hyphashop.shop.factory.ShopFactoryImpl;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.InvUI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class HyphaShopImpl extends HyphaShop {
    public static final @NotNull Logger LOGGER = new Logger();

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        InvUI.getInstance().setPlugin(this);

        if (ItemsAdderHook.isHooked()) {
            Bukkit.getPluginManager().registerEvents(new ItemsAdderListener(), INSTANCE);
        } else {
            init();
        }
    }

    @Override
    public void onDisable() {
        disable();
    }

    @Override
    public void init() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), INSTANCE);

//        if (!setupEconomy()) {
//            LogUtils.error("Plugin disabled due to no Vault dependency found!");
//            getServer().getPluginManager().disablePlugin(this);
//            return;
//        }

        PlaceholderAPIHook.load();
        MMOItemsHook.load();
        ItemsAdderHook.load();
        MythicMobsHook.load();

        setupBStats();

        enable();
    }

    @Override
    public void enable() {
        HyphaScript.init(INSTANCE, LOGGER);

        Config.load();

        if (!loadScripts()) {
            return;
        }

        MessageConfig.load();
        RarityConfig.load();
        ProductConfig.load();
        ShopConfig.load();
        try {
            CartGUIConfig.load();
            OrderHistoryGUIConfig.load();
        } catch (Exception e) {
            LOGGER.error("Error when parsing gui. Plugin will be disabled." + e.getMessage());
            setEnabled(false);
            return;
        }
        NormalGUIConfig.load();

        DATABASE_FACTORY = new DatabaseFactoryImpl();

        if (!DATABASE_FACTORY.getProvider().migrate().success) {
            LOGGER.error("Could not migrate database! Plugin will be disabled!");
            setEnabled(false);
            return;
        }

        CURRENCY_MANAGER = new CurrencyManagerImpl();
        PROFILE_FACTORY = new ProfileFactoryImpl();
        RARITY_FACTORY = new RarityFactoryImpl();
        PRODUCT_FACTORY = new ProductFactoryImpl();
        SHOP_FACTORY = new ShopFactoryImpl();
        NORMAL_GUI_FACTORY = new NormalGUIFactoryImpl();

        CONN_TASKS = new ConnTasksImpl();
    }

    @Override
    public void disable() {
        if (PROFILE_FACTORY != null)
            PROFILE_FACTORY.unload();
        if (SHOP_FACTORY != null)
            SHOP_FACTORY.unload();
        if (PRODUCT_FACTORY != null)
            PRODUCT_FACTORY.unload();
        if (NORMAL_GUI_FACTORY != null)
            NORMAL_GUI_FACTORY.unload();

        ScriptManager.unloadAllByOwner(INSTANCE.getName());
        clearInternalObjects();
    }

    @Override
    public void reload() {
        disable();
        enable();
    }

    @Override
    public void setupBStats() {
        final int pluginId = 21305;
        METRICS = new Metrics(this, pluginId);
    }

    public boolean loadScripts() {
        loadInternalObject(new HyphaShopBasicObject());
        loadInternalObject(new HyphaShopActionObject());

        try {
            Path scriptsPath = Paths.get(getDataFolder() + "/" + "scripts");
            LOGGER.info("About to load scripts from " + scriptsPath);
            loadScripsFromHps(scriptsPath);
            LOGGER.info("Successfully loaded scripts. Result global context is: ");
            LOGGER.info(InternalObjectManager.GLOBAL_OBJECT.toString());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to load scripts. HyphaShop will be disabled.");
            e.printStackTrace();
            setEnabled(false);
            return false;
        }
    }

    private static void loadScripsFromHps(@NotNull Path folder) throws IOException {
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            if (!folder.toFile().mkdirs()) {
                throw new RuntimeException("Error when creating scripts folder.");
            }
        }

        try (Stream<Path> paths = Files.walk(folder)) {
            paths.filter(path -> Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".hps"))
                    .forEach(path -> {
                        try {
                            ScriptManager.loadScript(path.toFile(), path.startsWith(folder + "/.global"), INSTANCE.getName());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private static void clearInternalObjects() {
        registeredMembers.forEach(InternalObjectManager.GLOBAL_OBJECT::deleteMember);
        registeredMembers.clear();
    }

    private static void loadInternalObject(@NotNull InternalObject object) {
        if (!Config.script_unpackInternalObject) {
            InternalObjectManager.register(object.getName(), object);
            registeredMembers.add(object.getName());
        } else
            object.getAsScriptObject().getExportedMembers().forEach((name, ref) -> {
                InternalObjectManager.GLOBAL_OBJECT.declareMember(name, ref);
                registeredMembers.add(name);
            });
    }
}
