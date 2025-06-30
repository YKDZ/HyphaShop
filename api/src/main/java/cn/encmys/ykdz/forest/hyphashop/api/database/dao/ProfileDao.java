package cn.encmys.ykdz.forest.hyphashop.api.database.dao;

import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ProfileSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ProfileDao {
    @Nullable ProfileSchema querySchema(@NotNull UUID playerUUID);

    void insertSchema(@NotNull ProfileSchema schema);

    void updateSchema(@NotNull ProfileSchema schema);

    void deleteSchema(@NotNull ProfileSchema schema);
}
