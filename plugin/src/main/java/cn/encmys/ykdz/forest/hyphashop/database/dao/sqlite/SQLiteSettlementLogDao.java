package cn.encmys.ykdz.forest.hyphashop.database.dao.sqlite;

import cn.encmys.ykdz.forest.hyphashop.api.HyphaShop;
import cn.encmys.ykdz.forest.hyphashop.api.database.dao.SettlementLogDao;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.SettlementLog;
import cn.encmys.ykdz.forest.hyphashop.api.shop.cashier.log.amount.AmountPair;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.enums.OrderType;
import cn.encmys.ykdz.forest.hyphashop.api.shop.order.record.ProductLocation;
import cn.encmys.ykdz.forest.hyphashop.shop.cashier.log.SettlementLogImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class SQLiteSettlementLogDao implements SettlementLogDao {
    private static @NotNull List<SettlementLog> parseSettlementLog(@NotNull ResultSet rs) throws SQLException {
        // 需要保持查询出的顺序
        final Map<Integer, SettlementLog> logs = new LinkedHashMap<>();

        while (rs.next()) {
            final int logId = rs.getInt("id");
            SettlementLog log = logs.get(logId);

            if (log == null) {
                log = new SettlementLogImpl(UUID.fromString(rs.getString("customer_uuid")), OrderType.valueOf(rs.getString("type")))
                        .setTransitionTime(new Date(rs.getTimestamp("transition_time").getTime()));
                logs.put(logId, log);
            }

            final Map<ProductLocation, AmountPair> orderedProducts = new HashMap<>(log.getOrderedProducts());
            final Map<ProductLocation, Double> pricePerStack = new HashMap<>(log.getPricePerStack());

            if (rs.getString("product_id") != null) {
                final ProductLocation loc = new ProductLocation(
                        rs.getString("shop_id"),
                        rs.getString("product_id")
                );
                final AmountPair pair = new AmountPair(
                        rs.getInt("product_amount"),
                        rs.getInt("ordered_stack")
                );
                orderedProducts.put(loc, pair);
                pricePerStack.put(loc, rs.getDouble("price_per_stack"));
            }

            log.setOrderedProducts(orderedProducts);
            log.setPricePerStack(pricePerStack);
        }
        return new ArrayList<>(logs.values());
    }

    @Override
    public @NotNull List<SettlementLog> queryLogs(@NotNull String shopId, @NotNull OrderType @NotNull ... types) {
        final String sql = "SELECT l.id, l.customer_uuid, l.type, l.transition_time, " +
                "lp.shop_id, lp.product_id, lp.product_amount, lp.ordered_stack, lp.price_per_stack " +
                "FROM hyphashop_settlement_log l " +
                "JOIN hyphashop_log_product lp ON l.id = lp.log_id " +
                "WHERE lp.shop_id = ? AND l.type IN (" + placeholders(types.length) + ") " +
                "ORDER BY l.transition_time DESC";

        try (final PreparedStatement stmt = HyphaShop.DATABASE_FACTORY.getConnection().prepareStatement(sql)) {
            stmt.setString(1, shopId);
            for (int i = 0; i < types.length; i++) {
                stmt.setString(i + 2, types[i].name());
            }

            return parseSettlementLog(stmt.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<SettlementLog> queryLogs(@NotNull UUID playerUUID, int offset, int limit, @NotNull OrderType @NotNull ... types) {
        final String sql = "SELECT l.id, l.customer_uuid, l.type, l.transition_time, " +
                "lp.shop_id, lp.product_id, lp.product_amount, lp.ordered_stack, lp.price_per_stack " +
                "FROM hyphashop_settlement_log l " +
                "LEFT JOIN hyphashop_log_product lp ON l.id = lp.log_id " +
                "WHERE l.customer_uuid = ? AND l.type IN (" + placeholders(types.length) + ") " +
                "ORDER BY l.transition_time DESC " +
                "LIMIT ? OFFSET ?";

        try (final Connection connection = HyphaShop.DATABASE_FACTORY.getConnection();
             final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            for (int i = 0; i < types.length; i++) {
                stmt.setString(i + 2, types[i].name());
            }
            stmt.setInt(types.length + 2, limit);
            stmt.setInt(types.length + 3, offset);

            return parseSettlementLog(stmt.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public int queryHistoryStack(@NotNull String productId, @NotNull OrderType @NotNull ... types) {
        final String sql = "SELECT SUM(history_stack) AS total FROM hyphashop_product_history " +
                "WHERE product_id = ? AND type IN (" + placeholders(types.length) + ")";

        try (final Connection connection = HyphaShop.DATABASE_FACTORY.getConnection();
             final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, productId);
            for (int i = 0; i < types.length; i++) {
                stmt.setString(i + 2, types[i].name());
            }

            final ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int queryHistoryAmount(@NotNull String productId, @NotNull OrderType @NotNull ... types) {
        final String sql = "SELECT SUM(history_amount) AS total FROM hyphashop_product_history " +
                "WHERE product_id = ? AND type IN (" + placeholders(types.length) + ")";

        try (final Connection connection = HyphaShop.DATABASE_FACTORY.getConnection();
             final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, productId);
            for (int i = 0; i < types.length; i++) {
                stmt.setString(i + 2, types[i].name());
            }

            final ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void insertLog(@NotNull SettlementLog log) {
        try (final Connection connection = HyphaShop.DATABASE_FACTORY.getConnection()) {
            connection.setAutoCommit(false);

            final String logSql = "INSERT INTO hyphashop_settlement_log (customer_uuid, type, transition_time) VALUES (?, ?, ?)";
            final int logId;
            try (PreparedStatement stmt = connection.prepareStatement(logSql)) {
                stmt.setString(1, log.getCustomerUUID().toString());
                stmt.setString(2, log.getType().name());
                stmt.setTimestamp(3, new Timestamp(log.getTransitionTime().getTime()));
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                logId = rs.next() ? rs.getInt(1) : -1;
            }

            final String productSql = "INSERT INTO hyphashop_log_product (log_id, shop_id, product_id, product_amount, ordered_stack, price_per_stack) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (final PreparedStatement stmt = connection.prepareStatement(productSql)) {
                for (final Map.Entry<ProductLocation, AmountPair> entry : log.getOrderedProducts().entrySet()) {
                    final ProductLocation loc = entry.getKey();
                    final AmountPair pair = entry.getValue();
                    stmt.setInt(1, logId);
                    stmt.setString(2, loc.shopId());
                    stmt.setString(3, loc.productId());
                    stmt.setInt(4, pair.amount());
                    stmt.setInt(5, pair.stack());
                    stmt.setDouble(6, log.getPricePerStack().get(loc));
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteLog(@NotNull UUID customerUUID, long daysLateThan) {
        final String sql = "DELETE FROM hyphashop_settlement_log " +
                "WHERE customer_uuid = ? AND transition_time < datetime('now', '-' || ? || ' days')";

        try (final Connection connection = HyphaShop.DATABASE_FACTORY.getConnection();
             final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customerUUID.toString());
            stmt.setLong(2, daysLateThan);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int countLog(@NotNull UUID customerUUID, @NotNull OrderType @NotNull ... types) {
        final String sql = "SELECT COUNT(*) AS count FROM hyphashop_settlement_log " +
                "WHERE customer_uuid = ? AND type IN (" + placeholders(types.length) + ")";

        try (final Connection connection = HyphaShop.DATABASE_FACTORY.getConnection();
             final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customerUUID.toString());
            for (int i = 0; i < types.length; i++) {
                stmt.setString(i + 2, types[i].name());
            }

            final ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("count") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Contract("_ -> new")
    private @NotNull String placeholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }
}