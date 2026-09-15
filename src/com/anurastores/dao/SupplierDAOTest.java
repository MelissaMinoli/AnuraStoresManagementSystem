package com.anurastores.dao;

import java.util.List;

import com.anurastores.model.Supplier;

public class SupplierDAOTest {

    public static void main(String[] args) {

        SupplierDAO supplierDAO =
                new SupplierDAO();

        List<Supplier> suppliers =
                supplierDAO.getAllSuppliers();

        for (Supplier supplier : suppliers) {

            System.out.println(
                    supplier.getSupplierId()
                    + " | "
                    + supplier.getSupplierName()
                    + " | "
                    + supplier.getPhone()
                    + " | "
                    + supplier.getStatus()
            );
        }
    }
}