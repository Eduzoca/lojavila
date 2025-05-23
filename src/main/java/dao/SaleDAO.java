package dao;

import model.UserSales;
import model.ProductSales;
import util.DBConnection;

import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SaleDAO {
    private static final DateTimeFormatter HOUR_FORMAT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");
    private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");

    public Map<LocalDate, Double> findSalesByDay(int days) throws SQLException {
        String sql =
                "SELECT DATE(sale_date) AS dt, SUM(quantity * unit_price) AS total " +
                        "FROM sales WHERE sale_date >= ? GROUP BY dt ORDER BY dt";
        LocalDate cutoff = LocalDate.now().minusDays(days);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(cutoff));
            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalDate, Double> map = new LinkedHashMap<>();
                while (rs.next()) {
                    map.put(rs.getDate("dt").toLocalDate(), rs.getDouble("total"));
                }
                return map;
            }
        }
    }

    public List<ProductSales> findTopSelling(int limit) throws SQLException {
        String sql =
                "SELECT p.name AS pname, SUM(s.quantity) AS total_qty " +
                        "FROM sales s " +
                        "JOIN products p ON s.product_id = p.id " +
                        "GROUP BY p.name " +
                        "ORDER BY total_qty DESC " +
                        "LIMIT ?";
        List<ProductSales> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProductSales(
                            rs.getString("pname"),
                            rs.getInt("total_qty")
                    ));
                }
            }
        }
        return list;
    }

    public Map<LocalDateTime, Double> findSalesByPeriod(Duration period) throws SQLException {
        String sql =
                "SELECT DATE_FORMAT(sale_date, '%Y-%m-%d %H:00:00') AS period, " +
                        "       SUM(quantity * unit_price) AS total " +
                        "FROM sales WHERE sale_date >= ? GROUP BY period ORDER BY period";
        LocalDateTime cutoff = LocalDateTime.now().minus(period);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(cutoff));
            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalDateTime, Double> map = new LinkedHashMap<>();
                while (rs.next()) {
                    String periodStr = rs.getString("period");
                    LocalDateTime dt = LocalDateTime.parse(periodStr, HOUR_FORMAT);
                    map.put(dt, rs.getDouble("total"));
                }
                return map;
            }
        }
    }

    public Map<LocalDateTime, Double> findSalesByMinute(int minutes) throws SQLException {
        String sql =
                "SELECT DATE_FORMAT(sale_date, '%Y-%m-%d %H:%i:00') AS period, " +
                        "       SUM(quantity * unit_price) AS total " +
                        "FROM sales WHERE sale_date >= ? GROUP BY period ORDER BY period";
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(cutoff));
            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalDateTime, Double> map = new LinkedHashMap<>();
                while (rs.next()) {
                    String periodStr = rs.getString("period");
                    LocalDateTime dt = LocalDateTime.parse(periodStr, MINUTE_FORMAT);
                    map.put(dt, rs.getDouble("total"));
                }
                return map;
            }
        }
    }

    public void save(int productId, int userId, int quantity, double unitPrice) {
        String sql = "INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) " +
                "VALUES (?,?,?,?,CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, userId);
            ps.setInt(3, quantity);
            ps.setDouble(4, unitPrice);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro salvando venda", e);
        }
    }

    public List<UserSales> findSalesByUser() throws SQLException {
        String sql =
                "SELECT u.username, SUM(s.quantity * s.unit_price) AS total_sold " +
                        "FROM sales s " +
                        "JOIN users u ON s.user_id = u.id " +
                        "GROUP BY u.username";
        List<UserSales> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new UserSales(
                        rs.getString("username"),
                        rs.getDouble("total_sold")
                ));
            }
        }
        return list;
    }
}
