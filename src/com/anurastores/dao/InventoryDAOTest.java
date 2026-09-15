package com.anurastores.dao;

import java.util.List;

import com.anurastores.model.Inventory;

public class InventoryDAOTest {

    public static void main(String[] args) {

        InventoryDAO inventoryDAO =
                new InventoryDAO();

        List<Inventory> inventoryList =
                inventoryDAO.getAllInventory();

        for (Inventory inventory :
                inventoryList) {

            System.out.println(
                    inventory.getProductId()
                    + " | "
                    + inventory.getProductName()
                    + " | Qty: "
                    + inventory.getQuantity()
                    + " | Reorder: "
                    + inventory.getReorderLevel()
                    + " | "
                    + inventory.getStockStatus()
            );
        }
    }
}