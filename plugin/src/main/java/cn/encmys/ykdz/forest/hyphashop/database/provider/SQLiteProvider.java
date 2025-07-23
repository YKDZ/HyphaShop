package cn.encmys.ykdz.forest.hyphashop.database.provider;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProductDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProfileDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.SettlementLogDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ShopDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.factory.enums.DBType;
import cn.encmys.ykdz.forest.hyphashop.api.database.provider.DBProvider;
import cn.encmys.ykdz.forest.hyphashop.api.utils.JarUtils;
import cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite.SQLiteProductDao;
import cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite.SQLiteProfileDao;
import cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite.SQLiteSettlementLogDao;
import cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite.SQLiteShopDao;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.File;

public class SQLiteProvider implements DBProvider {
    private final @NotNull DataSource dataSource;

    public SQLiteProvider() {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getJDBCUrl());
        dataSource = new HikariDataSource(config);
    }

    @Override
    public @NotNull DBType getType() {
        return DBType.SQLITE;
    }

    @Override
    public void init() {
        final File dbFile = new File(getDBFilePath());
        if (!dbFile.exists()) {
            HyphaShop.INSTANCE.saveResource("data/database.db", false);
        }
        JarUtils.saveMigrationFile("data/migrations/sqlite");
    }

    @Override
    public @NotNull DataSource getJDBCDataSource() {
        return dataSource;
    }

    @Override
    public @NotNull String getJDBCUrl() {
        return "jdbc:sqlite:" + getDBFilePath();
    }

    @Override
    public @NotNull MigrateResult migrate() {
        final File dir = new File(HyphaShop.INSTANCE.getDataFolder(), "/data/migrations/sqlite");
        final Flyway flyway = Flyway.configure()
                .dataSource(getJDBCUrl(), null, null)
                .locations("filesystem:" + dir.getAbsolutePath())
                .baselineOnMigrate(true)
                .load();
        return flyway.migrate();
    }

    @Override
    public @NotNull ProductDao getProductDao() {
        return new SQLiteProductDao();
    }

    @Override
    public @NotNull ProfileDao getProfileDao() {
        return new SQLiteProfileDao();
    }

    @Override
    public @NotNull SettlementLogDao getSettlementLogDao() {
        return new SQLiteSettlementLogDao();
    }

    @Override
    public @NotNull ShopDao getShopDao() {
        return new SQLiteShopDao();
    }

    private static @NotNull String getDBFilePath() {
        return HyphaShop.INSTANCE.getDataFolder() + "/data/database.db";
    }
}
