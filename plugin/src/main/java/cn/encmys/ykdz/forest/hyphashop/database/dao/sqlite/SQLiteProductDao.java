package cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProductDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ProductSchema;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SQLiteProductDao implements ProductDao {
    @Override
    public @Nullable ProductSchema querySchema(@NotNull String productId) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM hyphashop_product WHERE id = ?");
            pStmt.setString(1, productId);
            ResultSet rs = pStmt.executeQuery();
            if (rs.next()) {
                return new ProductSchema(
                        productId,
                        HyphaShop.GSON.fromJson(rs.getString("current_player_amount"), new TypeToken<Map<String, Integer>>() {
                        }.getType()),
                        rs.getInt("current_global_amount")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insertSchema(@NotNull ProductSchema schema) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("REPLACE INTO hyphashop_product VALUES (?, ?, ?)");
            pStmt.setString(1, schema.productId());
            pStmt.setString(2, HyphaShop.GSON.toJson(schema.currentPlayerAmount(), new TypeToken<Map<String, Integer>>() {
            }.getType()));
            pStmt.setInt(3, schema.currentGlobalAmount());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSchema(@NotNull ProductSchema schema) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("UPDATE hyphashop_product SET current_player_amount = ?, current_global_amount = ? WHERE id = ?");
            pStmt.setString(1, HyphaShop.GSON.toJson(schema.currentPlayerAmount(), new TypeToken<Map<String, Integer>>() {
            }.getType()));
            pStmt.setInt(2, schema.currentGlobalAmount());
            pStmt.setString(3, schema.productId());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSchema(@NotNull ProductSchema schema) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("DELETE FROM hyphashop_product WHERE id = ?");
            pStmt.setString(1, schema.productId());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
