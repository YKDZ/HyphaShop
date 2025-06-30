package cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.ShopDao;
import cn.encmys.ykdz.forest.hyphashop.api.database.schema.ShopSchema;
import cn.encmys.ykdz.forest.hyphashop.price.PricePairImpl;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SQLiteShopDao implements ShopDao {
    @Override
    public @Nullable ShopSchema querySchema(@NotNull String id) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM hyphashop_shop WHERE id = ?");
            pStmt.setString(1, id);
            ResultSet rs = pStmt.executeQuery();
            if (rs.next()) {
                return new ShopSchema(
                        id,
                        rs.getDouble("balance"),
                        HyphaShop.GSON.fromJson(rs.getString("cached_amounts"), new TypeToken<Map<String, Integer>>() {
                        }.getType()),
                        HyphaShop.GSON.fromJson(rs.getString("cached_prices"), new TypeToken<Map<String, PricePairImpl>>() {
                        }.getType()),
                        HyphaShop.GSON.fromJson(rs.getString("listed_products"), new TypeToken<List<String>>() {
                        }.getType()),
                        rs.getLong("last_restocking")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insertSchema(@NotNull ShopSchema schema) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("REPLACE INTO hyphashop_shop (id, balance, cached_amounts, cached_prices, listed_products, last_restocking) VALUES (?, ?, ?, ?, ?, ?)");
            pStmt.setString(1, schema.id());
            pStmt.setDouble(2, schema.balance());
            pStmt.setString(3, HyphaShop.GSON.toJson(schema.cachedAmounts(), new TypeToken<Map<String, Integer>>() {
            }.getType()));
            pStmt.setString(4, HyphaShop.GSON.toJson(schema.cachedPrices(), new TypeToken<Map<String, PricePairImpl>>() {
            }.getType()));
            pStmt.setString(5, HyphaShop.GSON.toJson(schema.listedProducts(), new TypeToken<List<String>>() {
            }.getType()));
            pStmt.setLong(6, schema.lastRestocking());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSchema(@NotNull ShopSchema schema) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("UPDATE hyphashop_shop SET balance = ?, cached_amounts = ?, cached_prices = ?, listed_products = ?, last_restocking = ? WHERE id = ?");
            pStmt.setString(1, schema.id());
            pStmt.setDouble(2, schema.balance());
            pStmt.setString(3, HyphaShop.GSON.toJson(schema.cachedAmounts(), new TypeToken<Map<String, Integer>>() {
            }.getType()));
            pStmt.setString(4, HyphaShop.GSON.toJson(schema.cachedPrices(), new TypeToken<Map<String, PricePairImpl>>() {
            }.getType()));
            pStmt.setString(5, HyphaShop.GSON.toJson(schema.listedProducts(), new TypeToken<List<String>>() {
            }.getType()));
            pStmt.setLong(6, schema.lastRestocking());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSchema(@NotNull ShopSchema schema) {
        try (Connection conn = HyphaShop.DATABASE_FACTORY.getConnection()) {
            PreparedStatement pStmt = conn.prepareStatement("DELETE FROM hyphashop_shop WHERE id = ?");
            pStmt.setString(1, schema.id());
            pStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
