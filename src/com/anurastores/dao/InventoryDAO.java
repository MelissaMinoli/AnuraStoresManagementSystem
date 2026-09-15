package com.anurastores.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.anurastores.model.Inventory;
import com.anurastores.model.StockMovement;
import com.anurastores.util.DBConnection;

public class InventoryDAO {

    // =====================================================
    // 1. VIEW ALL INVENTORY
    // =====================================================
    public List<Inventory> getAllInventory() {

        List<Inventory> inventoryList =
                new ArrayList<Inventory>();

        String sql =
                "SELECT i.inventory_id, "
                + "i.product_id, "
                + "p.product_name, "
                + "i.quantity, "
                + "p.reorder_level, "
                + "CASE "
                + "WHEN i.quantity = 0 THEN 'OUT OF STOCK' "
                + "WHEN i.quantity <= p.reorder_level THEN 'LOW STOCK' "
                + "ELSE 'IN STOCK' "
                + "END AS stock_status, "
                + "i.last_updated "
                + "FROM inventory i "
                + "INNER JOIN product p "
                + "ON i.product_id = p.product_id "
                + "ORDER BY p.product_name";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery()
        ) {

            while (result.next()) {

                Inventory inventory =
                        new Inventory(
                                result.getInt("inventory_id"),
                                result.getInt("product_id"),
                                result.getString("product_name"),
                                result.getInt("quantity"),
                                result.getInt("reorder_level"),
                                result.getString("stock_status"),
                                result.getTimestamp("last_updated")
                        );

                inventoryList.add(inventory);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return inventoryList;
    }


    // =====================================================
    // 2. SEARCH INVENTORY
    // =====================================================
    public List<Inventory> searchInventory(String keyword) {

        List<Inventory> inventoryList =
                new ArrayList<Inventory>();

        String sql =
                "SELECT i.inventory_id, "
                + "i.product_id, "
                + "p.product_name, "
                + "i.quantity, "
                + "p.reorder_level, "
                + "CASE "
                + "WHEN i.quantity = 0 THEN 'OUT OF STOCK' "
                + "WHEN i.quantity <= p.reorder_level THEN 'LOW STOCK' "
                + "ELSE 'IN STOCK' "
                + "END AS stock_status, "
                + "i.last_updated "
                + "FROM inventory i "
                + "INNER JOIN product p "
                + "ON i.product_id = p.product_id "
                + "WHERE CAST(i.product_id AS CHAR) LIKE ? "
                + "OR p.product_name LIKE ? "
                + "OR CASE "
                + "WHEN i.quantity = 0 THEN 'OUT OF STOCK' "
                + "WHEN i.quantity <= p.reorder_level THEN 'LOW STOCK' "
                + "ELSE 'IN STOCK' "
                + "END LIKE ? "
                + "ORDER BY p.product_name";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            String searchValue =
                    "%" + keyword + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);

            try (
                ResultSet result =
                        statement.executeQuery()
            ) {

                while (result.next()) {

                    Inventory inventory =
                            new Inventory(
                                    result.getInt("inventory_id"),
                                    result.getInt("product_id"),
                                    result.getString("product_name"),
                                    result.getInt("quantity"),
                                    result.getInt("reorder_level"),
                                    result.getString("stock_status"),
                                    result.getTimestamp("last_updated")
                            );

                    inventoryList.add(inventory);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return inventoryList;
    }


    // =====================================================
    // 3. ADD STOCK
    // quantity must be positive
    // =====================================================
    public boolean addStock(
            int productId,
            int quantity,
            String remarks) {

        if (quantity <= 0) {
            return false;
        }

        Connection connection = null;

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);


            // Check inventory row
            String checkSql =
                    "SELECT quantity "
                    + "FROM inventory "
                    + "WHERE product_id = ? "
                    + "FOR UPDATE";

            boolean inventoryExists = false;

            try (
                PreparedStatement checkStatement =
                        connection.prepareStatement(checkSql)
            ) {

                checkStatement.setInt(1, productId);

                try (
                    ResultSet result =
                            checkStatement.executeQuery()
                ) {

                    inventoryExists =
                            result.next();
                }
            }


            if (inventoryExists) {

                String updateSql =
                        "UPDATE inventory "
                        + "SET quantity = quantity + ? "
                        + "WHERE product_id = ?";

                try (
                    PreparedStatement updateStatement =
                            connection.prepareStatement(updateSql)
                ) {

                    updateStatement.setInt(1, quantity);
                    updateStatement.setInt(2, productId);

                    updateStatement.executeUpdate();
                }

            } else {

                String insertInventorySql =
                        "INSERT INTO inventory "
                        + "(product_id, quantity) "
                        + "VALUES (?, ?)";

                try (
                    PreparedStatement insertStatement =
                            connection.prepareStatement(
                                    insertInventorySql)
                ) {

                    insertStatement.setInt(1, productId);
                    insertStatement.setInt(2, quantity);

                    insertStatement.executeUpdate();
                }
            }


            // Record stock movement
            String movementSql =
                    "INSERT INTO stock_movement "
                    + "(product_id, movement_type, quantity, remarks) "
                    + "VALUES (?, 'STOCK_IN', ?, ?)";

            try (
                PreparedStatement movementStatement =
                        connection.prepareStatement(movementSql)
            ) {

                movementStatement.setInt(1, productId);
                movementStatement.setInt(2, quantity);
                movementStatement.setString(3, remarks);

                movementStatement.executeUpdate();
            }


            connection.commit();

            return true;

        } catch (Exception e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();

            return false;

        } finally {

            try {

                if (connection != null) {

                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // =====================================================
    // 4. ADJUST STOCK
    //
    // positive number  = increase
    // negative number  = decrease
    // example: 5 or -5
    // =====================================================
    public boolean adjustStock(
            int productId,
            int adjustmentQuantity,
            String remarks) {

        if (adjustmentQuantity == 0) {
            return false;
        }

        Connection connection = null;

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);


            String checkSql =
                    "SELECT quantity "
                    + "FROM inventory "
                    + "WHERE product_id = ? "
                    + "FOR UPDATE";

            int currentQuantity;

            try (
                PreparedStatement statement =
                        connection.prepareStatement(checkSql)
            ) {

                statement.setInt(1, productId);

                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    if (!result.next()) {

                        connection.rollback();

                        return false;
                    }

                    currentQuantity =
                            result.getInt("quantity");
                }
            }


            int newQuantity =
                    currentQuantity
                    + adjustmentQuantity;


            // Do not allow negative stock
            if (newQuantity < 0) {

                connection.rollback();

                return false;
            }


            String updateSql =
                    "UPDATE inventory "
                    + "SET quantity = ? "
                    + "WHERE product_id = ?";

            try (
                PreparedStatement statement =
                        connection.prepareStatement(updateSql)
            ) {

                statement.setInt(1, newQuantity);
                statement.setInt(2, productId);

                statement.executeUpdate();
            }


            String movementSql =
                    "INSERT INTO stock_movement "
                    + "(product_id, movement_type, quantity, remarks) "
                    + "VALUES (?, 'ADJUSTMENT', ?, ?)";

            try (
                PreparedStatement statement =
                        connection.prepareStatement(movementSql)
            ) {

                statement.setInt(1, productId);

                // Can be + or -
                statement.setInt(
                        2,
                        adjustmentQuantity);

                statement.setString(
                        3,
                        remarks);

                statement.executeUpdate();
            }


            connection.commit();

            return true;

        } catch (Exception e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();

            return false;

        } finally {

            try {

                if (connection != null) {

                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // =====================================================
    // 5. VIEW STOCK MOVEMENT HISTORY
    // =====================================================
    public List<StockMovement> getAllStockMovements() {

        List<StockMovement> movements =
                new ArrayList<StockMovement>();

        String sql =
                "SELECT sm.movement_id, "
                + "sm.product_id, "
                + "p.product_name, "
                + "sm.movement_type, "
                + "sm.quantity, "
                + "sm.movement_date, "
                + "sm.remarks "
                + "FROM stock_movement sm "
                + "INNER JOIN product p "
                + "ON sm.product_id = p.product_id "
                + "ORDER BY sm.movement_date DESC, "
                + "sm.movement_id DESC";

        try (
            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery()
        ) {

            while (result.next()) {

                StockMovement movement =
                        new StockMovement(
                                result.getInt("movement_id"),
                                result.getInt("product_id"),
                                result.getString("product_name"),
                                result.getString("movement_type"),
                                result.getInt("quantity"),
                                result.getTimestamp("movement_date"),
                                result.getString("remarks")
                        );

                movements.add(movement);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movements;
    }


    // =====================================================
    // 6. DELETE / REVERSE STOCK MOVEMENT
    //
    // SALE and SALE_CANCEL movements are protected.
    // =====================================================
    public boolean deleteStockMovement(
            int movementId) {

        Connection connection = null;

        try {

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);


            String movementSql =
                    "SELECT product_id, movement_type, quantity "
                    + "FROM stock_movement "
                    + "WHERE movement_id = ? "
                    + "FOR UPDATE";


            int productId;
            String movementType;
            int movementQuantity;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(movementSql)
            ) {

                statement.setInt(
                        1,
                        movementId);

                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    if (!result.next()) {

                        connection.rollback();

                        return false;
                    }

                    productId =
                            result.getInt("product_id");

                    movementType =
                            result.getString("movement_type");

                    movementQuantity =
                            result.getInt("quantity");
                }
            }


            // Finalized sales movements cannot be manually deleted
            if ("SALE".equalsIgnoreCase(movementType)
                    || "SALE_CANCEL".equalsIgnoreCase(
                            movementType)) {

                connection.rollback();

                return false;
            }


            String inventorySql =
                    "SELECT quantity "
                    + "FROM inventory "
                    + "WHERE product_id = ? "
                    + "FOR UPDATE";


            int currentQuantity;


            try (
                PreparedStatement statement =
                        connection.prepareStatement(inventorySql)
            ) {

                statement.setInt(1, productId);

                try (
                    ResultSet result =
                            statement.executeQuery()
                ) {

                    if (!result.next()) {

                        connection.rollback();

                        return false;
                    }

                    currentQuantity =
                            result.getInt("quantity");
                }
            }


            // Reverse the movement
            int correctedQuantity =
                    currentQuantity
                    - movementQuantity;


            if (correctedQuantity < 0) {

                connection.rollback();

                return false;
            }


            String updateSql =
                    "UPDATE inventory "
                    + "SET quantity = ? "
                    + "WHERE product_id = ?";

            try (
                PreparedStatement statement =
                        connection.prepareStatement(updateSql)
            ) {

                statement.setInt(
                        1,
                        correctedQuantity);

                statement.setInt(
                        2,
                        productId);

                statement.executeUpdate();
            }


            String deleteSql =
                    "DELETE FROM stock_movement "
                    + "WHERE movement_id = ?";

            try (
                PreparedStatement statement =
                        connection.prepareStatement(deleteSql)
            ) {

                statement.setInt(
                        1,
                        movementId);

                statement.executeUpdate();
            }


            connection.commit();

            return true;

        } catch (Exception e) {

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception rollbackException) {
                rollbackException.printStackTrace();
            }

            e.printStackTrace();

            return false;

        } finally {

            try {

                if (connection != null) {

                    connection.setAutoCommit(true);
                    connection.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}