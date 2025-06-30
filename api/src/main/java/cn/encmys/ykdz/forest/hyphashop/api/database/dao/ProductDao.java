package cn.encmys.ykdz.forest.hyphashop.api.database.dao;

import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ProductSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProductDao {
    @Nullable ProductSchema querySchema(@NotNull String productId);

    void insertSchema(@NotNull ProductSchema schema);

    void updateSchema(@NotNull ProductSchema schema);

    void deleteSchema(@NotNull ProductSchema schema);
}
