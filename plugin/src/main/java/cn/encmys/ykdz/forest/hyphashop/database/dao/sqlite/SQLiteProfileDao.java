package cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ProfileDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ProfileSchema;
import cn.encmys.ykdz.forest.hyphashop.api.profile.enums.ShoppingMode;
import cn.encmys.ykdz.forest.hyphashop.shop.order.ShopOrderImpl;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class SQLiteProfileDao implements ProfileDao {
    @Override
    public @Nullable ProfileSchema querySchema(@NotNull UUID playerUUID) {
        try (final Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            final PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM hyphashop_profile WHERE owner_uuid = ?");
            pStmt.setString(1, playerUUID.toString());
            final ResultSet rs = pStmt.executeQuery();
            if (rs.next()) {
                return new ProfileSchema(
                        playerUUID,
                        HyphaShop.GSON.fromJson(rs.getString("shopping_modes"), new TypeToken<Map<String, ShoppingMode>>() {
                        }.getType()),
                        HyphaShop.GSON.fromJson(rs.getString("cart_order"), ShopOrderImpl.class)
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insertSchema(@NotNull ProfileSchema schema) {
        try (final Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            final PreparedStatement pStmt = conn.prepareStatement("REPLACE INTO hyphashop_profile VALUES (?, ?, ?)");
            pStmt.setString(1, schema.ownerUUID().toString());
            pStmt.setString(2, HyphaShop.GSON.toJson(schema.shoppingModes(), new TypeToken<Map<String, ShoppingMode>>() {
            }.getType()));
            pStmt.setString(3, HyphaShop.GSON.toJson(schema.cartOrder(), ShopOrderImpl.class));
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSchema(@NotNull ProfileSchema schema) {
        try (final Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            final PreparedStatement pStmt = conn.prepareStatement("UPDATE hyphashop_profile SET shopping_modes = ?, cart_order = ? WHERE owner_uuid = ?");
            pStmt.setString(1, HyphaShop.GSON.toJson(schema.shoppingModes(), new TypeToken<Map<String, ShoppingMode>>() {
            }.getType()));
            pStmt.setString(2, HyphaShop.GSON.toJson(schema.cartOrder(), ShopOrderImpl.class));
            pStmt.setString(3, schema.ownerUUID().toString());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSchema(@NotNull ProfileSchema schema) {
        try (final Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            final PreparedStatement pStmt = conn.prepareStatement("DELETE FROM hyphashop_profile WHERE owner_uuid = ?");
            pStmt.setString(1, schema.ownerUUID().toString());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
