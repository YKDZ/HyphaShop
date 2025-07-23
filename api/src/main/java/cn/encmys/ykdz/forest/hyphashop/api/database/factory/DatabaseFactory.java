package cn.encmys.ykdz.forest.hyphashop.api.database.factory;

import cn.encmys.ykdz.forest.hyphashop.api.database.dao.*;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.enums.DBType;
import cn.encmys.ykdz.forest.hyphashop.api.database.provider.DBProvider;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseFactory {
    @NotNull DBProvider getProvider();

    @NotNull Connection getConnection() throws SQLException;

    @NotNull ProductDao getProductDao();

    @NotNull ProfileDao getProfileDao();

    @NotNull SettlementLogDao getSettlementLogDao();

    @NotNull ShopDao getShopDao();
}
