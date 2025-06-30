package cn.encmys.ykdz.forest.hyphashop;

import cn.encmys.ykdz.forest.hypharepo.factory.InternalObjectFactory;
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
import cn.encmys.ykdz.forest.hyphashop.script.pack.HyphaShopActionObject;
import cn.encmys.ykdz.forest.hyphashop.script.pack.HyphaShopBasicObject;
import cn.encmys.ykdz.forest.hyphashop.shop.factory.ShopFactoryImpl;
import cn.encmys.ykdz.forest.hyphashop.utils.LogUtils;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class HyphaShopImpl extends HyphaShop {
    @Override
    public void reload() {
        PROFILE_FACTORY.unload();
        SHOP_FACTORY.unload();
        PRODUCT_FACTORY.unload();
        NORMAL_GUI_FACTORY.unload();

        Config.load();
        MessageConfig.load();
        RarityConfig.load();
        ProductConfig.load();
        ShopConfig.load();
        CartGUIConfig.load();
        OrderHistoryGUIConfig.load();
        NormalGUIConfig.load();

        saveDefaultConfig();

        DATABASE_FACTORY = new DatabaseFactoryImpl();

        if (!DATABASE_FACTORY.migrate()) {
            setEnabled(false);
        }

        PROFILE_FACTORY = new ProfileFactoryImpl();
        RARITY_FACTORY = new RarityFactoryImpl();
        PRODUCT_FACTORY = new ProductFactoryImpl();
        SHOP_FACTORY = new ShopFactoryImpl();
        NORMAL_GUI_FACTORY = new NormalGUIFactoryImpl();
    }

    @Override
    public void onLoad() {
        INSTANCE = this;

        InternalObjectFactory.register(new HyphaShopBasicObject());
        InternalObjectFactory.register(new HyphaShopActionObject());
    }

    @Override
    public void onEnable() {
        if (ItemsAdderHook.isHooked()) {
            Bukkit.getPluginManager().registerEvents(new ItemsAdderListener(), INSTANCE);
        } else {
            init();
        }
    }

    @Override
    public void init() {
        if (isInitialized) return;
        isInitialized = true;

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

        Config.load();
        MessageConfig.load();
        RarityConfig.load();
        ProductConfig.load();
        ShopConfig.load();
        CartGUIConfig.load();
        OrderHistoryGUIConfig.load();
        NormalGUIConfig.load();

        DATABASE_FACTORY = new DatabaseFactoryImpl();

        if (!DATABASE_FACTORY.migrate()) {
            setEnabled(false);
        }

        PROFILE_FACTORY = new ProfileFactoryImpl();
        RARITY_FACTORY = new RarityFactoryImpl();
        PRODUCT_FACTORY = new ProductFactoryImpl();
        SHOP_FACTORY = new ShopFactoryImpl();
        NORMAL_GUI_FACTORY = new NormalGUIFactoryImpl();

        CONN_TASKS = new ConnTasksImpl();

        setupBStats();
    }

    @Override
    public void onDisable() {
        PROFILE_FACTORY.unload();
        SHOP_FACTORY.unload();
        PRODUCT_FACTORY.unload();
        NORMAL_GUI_FACTORY.unload();
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
}
