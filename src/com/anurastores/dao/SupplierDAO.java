package com.anurastores.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.anurastores.model.Supplier;
import com.anurastores.util.DBConnection;

public class SupplierDAO {

    // 1. ADD SUPPLIER
    public boolean addSupplier(Supplier supplier) {

        String sql = "INSERT INTO supplier "
                + "(supplier_name, contact_person, phone, email, address, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, supplier.getSupplierName());
            statement.setString(2, supplier.getContactPerson());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getAddress());
            statement.setString(6, supplier.getStatus());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // 2. VIEW ALL SUPPLIERS
    public List<Supplier> getAllSuppliers() {

        List<Supplier> suppliers =
                new ArrayList<Supplier>();

        String sql = "SELECT supplier_id, supplier_name, contact_person, "
                + "phone, email, address, status "
                + "FROM supplier "
                + "ORDER BY supplier_id DESC";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                Supplier supplier = new Supplier(
                        result.getInt("supplier_id"),
                        result.getString("supplier_name"),
                        result.getString("contact_person"),
                        result.getString("phone"),
                        result.getString("email"),
                        result.getString("address"),
                        result.getString("status")
                );

                suppliers.add(supplier);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return suppliers;
    }


    // 3. SEARCH SUPPLIER
    public List<Supplier> searchSuppliers(String keyword) {

        List<Supplier> suppliers =
                new ArrayList<Supplier>();

        String sql = "SELECT supplier_id, supplier_name, contact_person, "
                + "phone, email, address, status "
                + "FROM supplier "
                + "WHERE CAST(supplier_id AS CHAR) LIKE ? "
                + "OR supplier_name LIKE ? "
                + "OR contact_person LIKE ? "
                + "OR phone LIKE ? "
                + "OR email LIKE ? "
                + "OR status LIKE ? "
                + "ORDER BY supplier_id DESC";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            String searchValue =
                    "%" + keyword + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            statement.setString(4, searchValue);
            statement.setString(5, searchValue);
            statement.setString(6, searchValue);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Supplier supplier = new Supplier(
                            result.getInt("supplier_id"),
                            result.getString("supplier_name"),
                            result.getString("contact_person"),
                            result.getString("phone"),
                            result.getString("email"),
                            result.getString("address"),
                            result.getString("status")
                    );

                    suppliers.add(supplier);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return suppliers;
    }


    // 4. UPDATE SUPPLIER
    public boolean updateSupplier(Supplier supplier) {

        String sql = "UPDATE supplier SET "
                + "supplier_name = ?, "
                + "contact_person = ?, "
                + "phone = ?, "
                + "email = ?, "
                + "address = ?, "
                + "status = ? "
                + "WHERE supplier_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, supplier.getSupplierName());
            statement.setString(2, supplier.getContactPerson());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getAddress());
            statement.setString(6, supplier.getStatus());
            statement.setInt(7, supplier.getSupplierId());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // 5. DEACTIVATE SUPPLIER
    public boolean deactivateSupplier(int supplierId) {

        String sql = "UPDATE supplier "
                + "SET status = 'INACTIVE' "
                + "WHERE supplier_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, supplierId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // 6. CHECK DUPLICATE SUPPLIER
    public boolean supplierExists(
            String supplierName,
            String phone,
            int excludeSupplierId) {

        String sql = "SELECT supplier_id "
                + "FROM supplier "
                + "WHERE supplier_name = ? "
                + "AND phone = ? "
                + "AND supplier_id <> ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, supplierName);
            statement.setString(2, phone);
            statement.setInt(3, excludeSupplierId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}