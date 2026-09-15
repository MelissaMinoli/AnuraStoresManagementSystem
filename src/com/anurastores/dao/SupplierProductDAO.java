package com.anurastores.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.anurastores.model.SupplierProduct;
import com.anurastores.util.DBConnection;

public class SupplierProductDAO {

    // =====================================================
    // 1. ASSIGN PRODUCT TO SUPPLIER
    // =====================================================
    public boolean assignProductToSupplier(
            int supplierId,
            int productId) {

        if (supplierId <= 0 || productId <= 0) {
            return false;
        }

        // Only ACTIVE suppliers/products can be assigned
        if (!isSupplierActive(supplierId)
                || !isProductActive(productId)) {

            return false;
        }

        // Prevent duplicate relationship
        if (associationExists(
                supplierId,
                productId)) {

            return false;
        }

        String sql =
                "INSERT INTO supplier_product "
                + "(supplier_id, product_id) "
                + "VALUES (?, ?)";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    supplierId);

            statement.setInt(
                    2,
                    productId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 2. VIEW ALL SUPPLIER-PRODUCT RELATIONSHIPS
    // =====================================================
    public List<SupplierProduct> getAllSupplierProducts() {

        List<SupplierProduct> list =
                new ArrayList<SupplierProduct>();

        String sql =
                "SELECT sp.supplier_id, "
                + "s.supplier_name, "
                + "sp.product_id, "
                + "p.product_name "
                + "FROM supplier_product sp "
                + "INNER JOIN supplier s "
                + "ON sp.supplier_id = s.supplier_id "
                + "INNER JOIN product p "
                + "ON sp.product_id = p.product_id "
                + "ORDER BY s.supplier_name, p.product_name";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery()
        ) {

            while (result.next()) {

                list.add(
                        createSupplierProductFromResult(
                                result));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return list;
    }


    // =====================================================
    // 3. SEARCH SUPPLIER-PRODUCT RELATIONSHIPS
    // Supplier ID/name or Product ID/name
    // =====================================================
    public List<SupplierProduct> searchSupplierProducts(
            String keyword) {

        List<SupplierProduct> list =
                new ArrayList<SupplierProduct>();

        String sql =
                "SELECT sp.supplier_id, "
                + "s.supplier_name, "
                + "sp.product_id, "
                + "p.product_name "
                + "FROM supplier_product sp "
                + "INNER JOIN supplier s "
                + "ON sp.supplier_id = s.supplier_id "
                + "INNER JOIN product p "
                + "ON sp.product_id = p.product_id "
                + "WHERE CAST(sp.supplier_id AS CHAR) LIKE ? "
                + "OR s.supplier_name LIKE ? "
                + "OR CAST(sp.product_id AS CHAR) LIKE ? "
                + "OR p.product_name LIKE ? "
                + "ORDER BY s.supplier_name, p.product_name";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            String value =
                    "%" + keyword + "%";

            statement.setString(1, value);
            statement.setString(2, value);
            statement.setString(3, value);
            statement.setString(4, value);

            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                while (result.next()) {

                    list.add(
                            createSupplierProductFromResult(
                                    result));
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return list;
    }


    // =====================================================
    // 4. GET PRODUCTS FOR ONE SUPPLIER
    // =====================================================
    public List<SupplierProduct> getProductsBySupplier(
            int supplierId) {

        List<SupplierProduct> list =
                new ArrayList<SupplierProduct>();

        String sql =
                "SELECT sp.supplier_id, "
                + "s.supplier_name, "
                + "sp.product_id, "
                + "p.product_name "
                + "FROM supplier_product sp "
                + "INNER JOIN supplier s "
                + "ON sp.supplier_id = s.supplier_id "
                + "INNER JOIN product p "
                + "ON sp.product_id = p.product_id "
                + "WHERE sp.supplier_id = ? "
                + "ORDER BY p.product_name";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    supplierId);

            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                while (result.next()) {

                    list.add(
                            createSupplierProductFromResult(
                                    result));
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return list;
    }


    // =====================================================
    // 5. REMOVE PRODUCT FROM SUPPLIER
    // Only relationship is removed.
    // Product/Supplier records are NOT deleted.
    // =====================================================
    public boolean removeSupplierProduct(
            int supplierId,
            int productId) {

        if (supplierId <= 0 || productId <= 0) {
            return false;
        }

        String sql =
                "DELETE FROM supplier_product "
                + "WHERE supplier_id = ? "
                + "AND product_id = ?";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    supplierId);

            statement.setInt(
                    2,
                    productId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 6. DUPLICATE ASSOCIATION CHECK
    // =====================================================
    public boolean associationExists(
            int supplierId,
            int productId) {

        String sql =
                "SELECT supplier_id "
                + "FROM supplier_product "
                + "WHERE supplier_id = ? "
                + "AND product_id = ? "
                + "LIMIT 1";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    supplierId);

            statement.setInt(
                    2,
                    productId);

            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                return result.next();
            }

        } catch (Exception e) {

            e.printStackTrace();

            // Safer to prevent assignment
            // when duplicate checking fails.
            return true;
        }
    }


    // =====================================================
    // 7. CHECK ACTIVE SUPPLIER
    // =====================================================
    private boolean isSupplierActive(
            int supplierId) {

        String sql =
                "SELECT supplier_id "
                + "FROM supplier "
                + "WHERE supplier_id = ? "
                + "AND status = 'ACTIVE'";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    supplierId);

            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                return result.next();
            }

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // 8. CHECK ACTIVE PRODUCT
    // =====================================================
    private boolean isProductActive(
            int productId) {

        String sql =
                "SELECT product_id "
                + "FROM product "
                + "WHERE product_id = ? "
                + "AND status = 'ACTIVE'";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    productId);

            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                return result.next();
            }

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }


    // =====================================================
    // RESULTSET -> SUPPLIER PRODUCT
    // =====================================================
    private SupplierProduct createSupplierProductFromResult(
            ResultSet result) throws Exception {

        return new SupplierProduct(
                result.getInt("supplier_id"),
                result.getString("supplier_name"),
                result.getInt("product_id"),
                result.getString("product_name")
        );
    }
}