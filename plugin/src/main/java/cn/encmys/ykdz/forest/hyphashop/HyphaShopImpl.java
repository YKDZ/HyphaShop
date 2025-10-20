package cn.encmys.ykdz.forest.hyphashop;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.ScriptManager;
import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.config.*;
import cn.encmys.ykdz.forest.hyphashop.database.factory.DatabaseFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.gui.factory.NormalGUIFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.hook.ItemsAdderHook;
import cn.encmys.ykdz.forest.hyphashop.hook.MMOItemsHook;
import cn.encmys.ykdz.forest.hyphashop.hook.MythicMobsHook;
import cn.encmys.ykdz.forest.hyphashop.hook.PlaceholderAPIHook;
import cn.encmys.ykdz.forest.hyphashop.listener.ItemsAdderListener;
import cn.encmys.ykdz.forest.hyphashop.listener.PlayerListener;
import cn.encmys.ykdz.forest.hyphashop.product.factory.ProductFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.profile.factory.ProfileFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.rarity.factory.RarityFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.scheduler.ConnTasksImpl;
import cn.encmys.ykdz.forest.hyphashop.script.pack.*;
import cn.encmys.ykdz.forest.hyphashop.shop.factory.ShopFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.InvUI;

import java.nio.file.Paths;

public final class HyphaShopImpl extends HyphaShop {
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

        if (!setupEconomy()) {
            LogUtils.error("Plugin disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PlaceholderAPIHook.load();
        MMOItemsHook.load();
        ItemsAdderHook.load();
        MythicMobsHook.load();

        setupBStats();

        enable();
    }

    @Override
    public void enable() {
        Config.load();

        loadScripts();

        MessageConfig.load();
        RarityConfig.load();
        ProductConfig.load();
        ShopConfig.load();
        CartGUIConfig.load();
        OrderHistoryGUIConfig.load();
        NormalGUIConfig.load();

        DATABASE_FACTORY = new DatabaseFactoryImpl();

        if (!DATABASE_FACTORY.getProvider().migrate().success) {
            setEnabled(false);
        }

        PROFILE_FACTORY = new ProfileFactoryImpl();
        RARITY_FACTORY = new RarityFactoryImpl();
        PRODUCT_FACTORY = new ProductFactoryImpl();
        SHOP_FACTORY = new ShopFactoryImpl();
        NORMAL_GUI_FACTORY = new NormalGUIFactoryImpl();

        CONN_TASKS = new ConnTasksImpl();
    }

    @Override
    public void disable() {
        PROFILE_FACTORY.unload();
        SHOP_FACTORY.unload();
        PRODUCT_FACTORY.unload();
        NORMAL_GUI_FACTORY.unload();

        ScriptManager.unloadAllScripts();
        clearInternalObjects();
    }

    @Override
    public void reload() {
        disable();
        enable();
    }

    @Override
    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        ECONOMY = rsp.getProvider();

        return true;
    }

    @Override
    public void setupBStats() {
        final int pluginId = 21305;
        METRICS = new Metrics(this, pluginId);
    }

    public void loadScripts() {
        loadInternalObject(new ConsoleObject());
        loadInternalObject(new PlaceholderAPIObject());
        loadInternalObject(new PlayerObject());
        loadInternalObject(new ServerObject());
        loadInternalObject(new CommandObject());
        loadInternalObject(new HyphaShopBasicObject());
        loadInternalObject(new HyphaShopActionObject());

        try {
            ScriptManager.loadAllFrom(Paths.get(getDataFolder() + "/" + "scripts"));
            LogUtils.info("Successfully loaded scripts. Result global context is: ");
            LogUtils.info(Context.GLOBAL_OBJECT.toString());
        } catch (Exception e) {
            LogUtils.error("Failed to load scripts. HyphaShop will be disabled.");
            setEnabled(false);
            e.printStackTrace();
        }
    }

    private static void clearInternalObjects() {
        registeredMembers.forEach(Context.GLOBAL_OBJECT::deleteMember);
        registeredMembers.clear();
    }

    private static void loadInternalObject(@NotNull InternalObject object) {
        if (!Config.script_unpackInternalObject) {
            InternalObjectManager.register(object.getName(), object);
            registeredMembers.add(object.getName());
        } else object.getAsScriptObject().getExportedMembers().forEach((name, ref) -> {
            Context.GLOBAL_OBJECT.declareMember(name, ref);
            registeredMembers.add(name);
        });
    }
}
