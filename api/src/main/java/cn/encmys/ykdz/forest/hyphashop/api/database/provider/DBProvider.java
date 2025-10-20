package cn.encmys.ykdz.forest.hyphashop.api.database.provider;

import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProductDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProfileDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.SettlementLogDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ShopDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.enums.DBType;
import org.flywaydb.core.api.output.MigrateResult;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;

public interface DBProvider {
    @NotNull DBType getType();

    @NotNull DataSource getJDBCDataSource();

    @NotNull String getJDBCUrl();

    @NotNull MigrateResult migrate();

    @NotNull ProductDao getProductDao();

    @NotNull ProfileDao getProfileDao();

    @NotNull SettlementLogDao getSettlementLogDao();

    @NotNull ShopDao getShopDao();
}
