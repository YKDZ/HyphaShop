package cn.encmys.ykdz.forest.hyphashop.api;

import cn.encmys.ykdz.forest.hyphashop.api.database.factory.DatabaseFactory;
import cn.encmys.ykdz.forest.hyphashop.api.gui.factory.NormalGUIFactory;
import cn.encmys.ykdz.forest.hyphashop.api.product.factory.ProductFactory;
import cn.encmys.ykdz.forest.hyphashop.api.profile.factory.ProfileFactory;
import cn.encmys.ykdz.forest.hyphashop.api.rarity.factory.RarityFactory;
import cn.encmys.ykdz.forest.hyphashop.api.scheduler.ConnTasks;
import cn.encmys.ykdz.forest.hyphashop.api.shop.factory.ShopFactory;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class HyphaShop extends JavaPlugin {
    public final static @NotNull Gson GSON = new GsonBuilder()
            // 因为内部使用 NaN 表示无效值
            .serializeSpecialFloatingPointValues()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(ProductLocation.class, new ProductLocation.Adapter())
            .create();
    public static HyphaShop INSTANCE;
    public static ProfileFactory PROFILE_FACTORY;
    public static RarityFactory RARITY_FACTORY;
    public static ProductFactory PRODUCT_FACTORY;
    public static ShopFactory SHOP_FACTORY;
    public static ConnTasks CONN_TASKS;
    public static DatabaseFactory DATABASE_FACTORY;
    public static NormalGUIFactory NORMAL_GUI_FACTORY;
    public static Economy ECONOMY;
    public static Metrics METRICS;
    public static final List<String> registeredMembers = new ArrayList<>();

    public abstract void disable();

    public abstract void reload();

    public abstract void init();

    public abstract void enable();

    public abstract boolean setupEconomy();

    public abstract void setupBStats();

    public abstract boolean loadScripts();
}
