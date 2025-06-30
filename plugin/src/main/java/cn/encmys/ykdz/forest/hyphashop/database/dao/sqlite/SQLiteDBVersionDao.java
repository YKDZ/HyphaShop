package cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.DBVersionDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.schema.DBVersionSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

public class SQLiteDBVersionDao implements DBVersionDao {
    @Override
    public @Nullable DBVersionSchema queryLatestSchema() throws SQLException {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM hyphashop_db_version ORDER BY latest_migration_time DESC LIMIT 1");
            ResultSet rs = pStmt.executeQuery();
            if (rs.next()) {
                return new DBVersionSchema(
                        rs.getInt("version"),
                        rs.getInt("migration_from_version"),
                        rs.getTime("latest_migration_time")
                );
            }
        }
        return null;
    }

    @Override
    public void insertSchema(@NotNull DBVersionSchema schema) throws SQLException {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("INSERT INTO hyphashop_db_version VALUES (?, ?, ?)");
            pStmt.setInt(1, schema.version());
            pStmt.setInt(2, schema.migrationFromVersion());
            pStmt.setTimestamp(3, new Timestamp(schema.latestMigrationTime().getTime()));
            pStmt.executeUpdate();
        }
    }
}
