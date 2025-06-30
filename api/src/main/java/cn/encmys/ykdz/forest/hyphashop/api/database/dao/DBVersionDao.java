package cn.encmys.ykdz.forest.hyphashop.api.database.dao;

import cn.encmys.ykdz.forest.hyphashop.api.database.schema.DBVersionSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public interface DBVersionDao {
    @Nullable DBVersionSchema queryLatestSchema() throws SQLException;

    void insertSchema(@NotNull DBVersionSchema schema) throws SQLException;
}
