package cn.encmys.ykdz.forest.dailyshop;

import cn.encmys.ykdz.forest.dailyshop.adventure.AdventureManagerImpl;
import cn.encmys.ykdz.forest.dailyshop.api.DailyShop;
import cn.encmys.ykdz.forest.dailyshop.api.config.*;
import cn.encmys.ykdz.forest.dailyshop.api.utils.LogUtils;
import cn.encmys.ykdz.forest.dailyshop.command.CommandHandler;
import cn.encmys.ykdz.forest.dailyshop.database.SQLiteDatabase;
import cn.encmys.ykdz.forest.dailyshop.hook.ItemsAdderHook;
import cn.encmys.ykdz.forest.dailyshop.hook.MMOItemsHook;
import cn.encmys.ykdz.forest.dailyshop.hook.MythicMobsHook;
import cn.encmys.ykdz.forest.dailyshop.hook.PlaceholderAPIHook;
import cn.encmys.ykdz.forest.dailyshop.product.factory.ProductFactoryImpl;
import cn.encmys.ykdz.forest.dailyshop.rarity.factory.RarityFactoryImpl;
import cn.encmys.ykdz.forest.dailyshop.scheduler.SchedulerImpl;
import cn.encmys.ykdz.forest.dailyshop.shop.factory.ShopFactoryImpl;
import cn.encmys.ykdz.forest.dailyshop.shop.order.builder.ShopOrderBuilderImpl;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class DailyShopImpl extends DailyShop {
    @Override
    public void reload() {
        DailyShop.SHOP_FACTORY.unload();
        DailyShop.PRODUCT_FACTORY.unload();

        Config.load();
        LocateConfig.load();
        MessageConfig.load();
        RarityConfig.load();
        ProductConfig.load();
        ShopConfig.load();

        DailyShop.RARITY_FACTORY = new RarityFactoryImpl();
        DailyShop.PRODUCT_FACTORY = new ProductFactoryImpl();
        DailyShop.SHOP_FACTORY = new ShopFactoryImpl();
        DailyShop.SHOP_ORDER_BUILDER = new ShopOrderBuilderImpl();
    }

    @Override
    public void init() {
        DailyShopImpl.RARITY_FACTORY = new RarityFactoryImpl();
        DailyShopImpl.PRODUCT_FACTORY = new ProductFactoryImpl();
        DailyShopImpl.SHOP_FACTORY = new ShopFactoryImpl();
        DailyShop.SHOP_ORDER_BUILDER = new ShopOrderBuilderImpl();

        DailyShopImpl.SCHEDULER = new SchedulerImpl();
    }

    @Override
    public void onLoad() {
        INSTANCE = this;

        CommandAPI.onLoad(new CommandAPIBukkitConfig(INSTANCE));
    }

    @Override
    public void onEnable() {
        if (ItemsAdderHook.isHooked()) {
            Bukkit.getPluginManager().registerEvents(this, INSTANCE);
        }

        ADVENTURE_MANAGER = new AdventureManagerImpl(INSTANCE);

        if (!setupEconomy()) {
            LogUtils.error("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new PlaceholderAPIHook();
        new MMOItemsHook();
        new ItemsAdderHook();
        new MythicMobsHook();

        Config.load();
        MessageConfig.load();
        RarityConfig.load();
        ProductConfig.load();
        ShopConfig.load();

        DATABASE = new SQLiteDatabase();

        if (!ItemsAdderHook.isHooked()) {
            init();
        }

        CommandAPI.onEnable();
        new CommandHandler(INSTANCE).load();

        setupBStats();
    }

    @Override
    @EventHandler
    public void waitForItemsAdder(ItemsAdderLoadDataEvent e) {
        init();
    }

    @Override
    public void onDisable() {
        SHOP_FACTORY.unload();
        PRODUCT_FACTORY.unload();

        CommandAPI.onDisable();
    }

    @Override
    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        DailyShopImpl.ECONOMY = rsp.getProvider();
        return true;
    }

    @Override
    public boolean setupBStats() {
        int pluginId = 21305;
        DailyShopImpl.METRICS = new Metrics(this, pluginId);
        return true;
    }
}
