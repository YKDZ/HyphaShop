package cn.encmys.ykdz.forest.hyphashop.api.database.dao;

import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ShopSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ShopDao {
    @Nullable ShopSchema querySchema(@NotNull String shopId);

    void insertSchema(@NotNull ShopSchema schema);

    void updateSchema(@NotNull ShopSchema schema);

    void deleteSchema(@NotNull ShopSchema schema);
}
