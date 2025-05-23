package dao;

import model.Product;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> findAll() {
        String sql = "SELECT id, name, price, quantity FROM products ORDER BY id";
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro listando produtos", e);
        }
        return list;
    }

    public void save(Product product) {
        String sql = "INSERT INTO products(name, price, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro inserindo produto", e);
        }
    }

    public void update(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo para atualização");
        }
        String sql = "UPDATE products SET name = ?, price = ?, quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setInt(3, product.getQuantity());
            ps.setInt(4, product.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro atualizando produto", e);
        }
    }

    public void delete(Integer id) {
        if (id == null) return;
        String delSales   = "DELETE FROM sales   WHERE product_id = ?";
        String delProduct = "DELETE FROM products WHERE id         = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psSales   = conn.prepareStatement(delSales);
             PreparedStatement psProduct = conn.prepareStatement(delProduct)) {

            conn.setAutoCommit(false);

            // 1) remover vendas associadas
            psSales.setInt(1, id);
            psSales.executeUpdate();

            // 2) remover produto
            psProduct.setInt(1, id);
            psProduct.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            // tenta rollback em caso de falha
            try { Connection conn = DBConnection.getConnection(); conn.rollback(); } catch (Exception ignore) {}
            throw new RuntimeException("Erro deletando produto em cascata", e);
        }
    }

    public Product findById(Integer id) {
        if (id == null) return null;
        String sql = "SELECT id, name, price, quantity FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando produto por ID", e);
        }
        return null;
    }

    public Product findByName(String name) {
        String sql = "SELECT id, name, price, quantity FROM products WHERE name = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro buscando produto por nome", e);
        }
        return null;
    }
}
