package cn.encmys.ykdz.forest.hyphashop.database.factory;

import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProductDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProfileDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.SettlementLogDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ShopDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.DatabaseFactory;
import cn.encmys.ykdz.forest.hyphashop.api.database.provider.DBProvider;
import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphashop.database.provider.SQLiteProvider;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseFactoryImpl implements DatabaseFactory {
    private DBProvider provider;
    private ProductDao productDao;
    private ProfileDao profileDao;
    private SettlementLogDao settlementLogDao;
    private ShopDao shopDao;

    public DatabaseFactoryImpl() {
        if (Config.database_sqlite_enabled) {
            provider = new SQLiteProvider();
        } else if (Config.database_mysql_enabled) {
            // TODO MySQL 实现
            throw new RuntimeException("MySQL not implemented yet");
        }
        assert provider != null;
    }

    @Override
    public @NotNull DBProvider getProvider() {
        return provider;
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return provider.getJDBCDataSource().getConnection();
    }

    @Override
    public @NotNull ProductDao getProductDao() {
        if (productDao == null) {
            productDao = provider.getProductDao();
        }
        return productDao;
    }

    @Override
    public @NotNull ProfileDao getProfileDao() {
        if (profileDao == null) {
            profileDao = provider.getProfileDao();
        }
        return profileDao;
    }

    @Override
    public @NotNull SettlementLogDao getSettlementLogDao() {
        if (settlementLogDao == null) {
            settlementLogDao = provider.getSettlementLogDao();
        }
        return settlementLogDao;
    }

    @Override
    public @NotNull ShopDao getShopDao() {
        if (shopDao == null) {
            shopDao = provider.getShopDao();
        }
        return shopDao;
    }
}
