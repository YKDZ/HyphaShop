package cn.encmys.ykdz.forest.hyphashop.database.factory;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.*;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.DatabaseFactory;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.enums.DBType;
import cn.encmys.ykdz.forest.hyphashop.config.Config;
import cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite.*;
import cn.encmys.ykdz.forest.hyphashop.database.migration.MigrationHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseFactoryImpl implements DatabaseFactory {
    private DataSource dataSource;
    private DBType dbType;
    private ProductDao productDao;
    private ProfileDao profileDao;
    private SettlementLogDao settlementLogDao;
    private ShopDao shopDao;
    private DBVersionDao dbVersionDao;

    public DatabaseFactoryImpl() {
        load();
    }

    public void load() {
        if (Config.database_sqlite_enabled) {
            dbType = DBType.SQLITE;
            loadSQLite();
        } else if (Config.database_mysql_enabled) {
            dbType = DBType.MYSQL;
            // TODO MySQL 实现
        }
    }

    @Override
    public void loadSQLite() {
        final HikariConfig config = new HikariConfig();
        final String path = HyphaShop.INSTANCE.getDataFolder() + "/data/database.db";
        final File dbFile = new File(path);
        if (!dbFile.exists()) {
            HyphaShop.INSTANCE.saveResource("data/database.db", false);
        }
        config.setJdbcUrl("jdbc:sqlite:" + path);
        dataSource = new HikariDataSource(config);
    }

    @Override
    public boolean migrate() {
        switch (dbType) {
            case SQLITE:
                return new MigrationHandler(DBType.SQLITE).migrate();
            case MYSQL:
                // TODO MYSQL 实现
        }
        return false;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public ProductDao getProductDao() {
        if (productDao == null) {
            switch (dbType) {
                case SQLITE -> productDao = new SQLiteProductDao();
            }
        }
        return productDao;
    }

    @Override
    public ProfileDao getProfileDao() {
        if (profileDao == null) {
            switch (dbType) {
                case SQLITE -> profileDao = new SQLiteProfileDao();
            }
        }
        return profileDao;
    }

    @Override
    public SettlementLogDao getSettlementLogDao() {
        if (settlementLogDao == null) {
            switch (dbType) {
                case SQLITE -> settlementLogDao = new SQLiteSettlementLogDao();
            }
        }
        return settlementLogDao;
    }

    @Override
    public ShopDao getShopDao() {
        if (shopDao == null) {
            switch (dbType) {
                case SQLITE -> shopDao = new SQLiteShopDao();
            }
        }
        return shopDao;
    }

    @Override
    public DBVersionDao getDBVersionDao() {
        if (dbVersionDao == null) {
            switch (dbType) {
                case SQLITE -> dbVersionDao = new SQLiteDBVersionDao();
            }
        }
        return dbVersionDao;
    }

    @Override
    public DBType getDbType() {
        return dbType;
    }
}
