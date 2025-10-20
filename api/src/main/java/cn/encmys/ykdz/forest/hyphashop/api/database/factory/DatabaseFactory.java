package cn.encmys.ykdz.forest.hyphashop.api.database.factory;

import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProductDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProfileDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.SettlementLogDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ShopDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.provider.DBProvider;
import org.jetbrains.annotations.NotNull;

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
