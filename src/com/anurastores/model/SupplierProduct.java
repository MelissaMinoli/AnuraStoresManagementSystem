package com.anurastores.model;

public class SupplierProduct {

    private int supplierId;
    private String supplierName;

    private int productId;
    private String productName;


    public SupplierProduct() {
    }


    public SupplierProduct(
            int supplierId,
            String supplierName,
            int productId,
            String productName) {

        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.productId = productId;
        this.productName = productName;
    }


    public int getSupplierId() {
        return supplierId;
    }


    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }


    public String getSupplierName() {
        return supplierName;
    }


    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
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
}