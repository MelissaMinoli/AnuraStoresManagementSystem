package com.anurastores.model;

import java.sql.Timestamp;

public class Inventory {

    private int inventoryId;
    private int productId;
    private String productName;
    private int quantity;
    private int reorderLevel;
    private String stockStatus;
    private Timestamp lastUpdated;

    public Inventory() {
    }

    public Inventory(int inventoryId,
                     int productId,
                     String productName,
                     int quantity,
                     int reorderLevel,
                     String stockStatus,
                     Timestamp lastUpdated) {

        this.inventoryId = inventoryId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}