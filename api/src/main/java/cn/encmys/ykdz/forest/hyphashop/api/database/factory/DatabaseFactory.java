package cn.encmys.ykdz.forest.hyphashop.api.database.factory;

import cn.encmys.ykdz.forest.hyphashop.api.database.dao.*;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.enums.DBType;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseFactory {
    void loadSQLite();

    boolean migrate();

    Connection getConnection() throws SQLException;

    ProductDao getProductDao();

    ProfileDao getProfileDao();

    SettlementLogDao getSettlementLogDao();

    ShopDao getShopDao();

    DBVersionDao getDBVersionDao();

    DBType getDbType();
}
