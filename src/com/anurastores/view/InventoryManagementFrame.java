package com.anurastores.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.anurastores.dao.InventoryDAO;
import com.anurastores.dao.ProductDAO;
import com.anurastores.model.Inventory;
import com.anurastores.model.Product;
import com.anurastores.model.StockMovement;
import com.anurastores.model.User;

public class InventoryManagementFrame extends JFrame {

    private JComboBox<ProductItem> productCombo;
    private JTextField quantityField;
    private JTextField remarksField;
    private JTextField searchField;

    private JLabel currentStockLabel;
    private JLabel lowStockLabel;

    private JTable inventoryTable;
    private JTable movementTable;

    private DefaultTableModel inventoryTableModel;
    private DefaultTableModel movementTableModel;

    private InventoryDAO inventoryDAO;
    private ProductDAO productDAO;

    private User loggedInUser;

    private JButton deleteMovementButton;

    public InventoryManagementFrame(User user) {

        this.loggedInUser = user;

        inventoryDAO = new InventoryDAO();
        productDAO = new ProductDAO();

        setTitle("Inventory Management - Anura Stores");
        setSize(1250, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();

        loadProducts();
        loadInventory();
        loadStockMovements();
    }

    private void initComponents() {

        // ==========================
        // STOCK FORM
        // ==========================

        productCombo = new JComboBox<ProductItem>();

        quantityField = new JTextField(8);
        remarksField = new JTextField(18);

        currentStockLabel =
                new JLabel("Current Stock: -");

        lowStockLabel =
                new JLabel("Low Stock Items: 0");


        JButton addStockButton =
                new JButton("Add Stock");

        JButton adjustStockButton =
                new JButton("Adjust Stock");

        JButton clearButton =
                new JButton("Clear");


        JPanel stockPanel =
                new JPanel();

        stockPanel.add(
                new JLabel("Product:"));

        stockPanel.add(productCombo);

        stockPanel.add(
                new JLabel("Quantity:"));

        stockPanel.add(quantityField);

        stockPanel.add(
                new JLabel("Remarks:"));

        stockPanel.add(remarksField);

        stockPanel.add(addStockButton);
        stockPanel.add(adjustStockButton);
        stockPanel.add(clearButton);


        // ==========================
        // SEARCH
        // ==========================

        searchField =
                new JTextField(15);

        JButton searchButton =
                new JButton("Search");

        JButton showAllButton =
                new JButton("Show All");


        JPanel searchPanel =
                new JPanel();

        searchPanel.add(
                new JLabel("Search Inventory:"));

        searchPanel.add(searchField);

        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        searchPanel.add(currentStockLabel);

        searchPanel.add(lowStockLabel);


        JPanel topPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1));

        topPanel.add(stockPanel);
        topPanel.add(searchPanel);


        // ==========================
        // INVENTORY TABLE
        // ==========================

        String[] inventoryColumns = {

                "Inventory ID",
                "Product ID",
                "Product Name",
                "Quantity",
                "Reorder Level",
                "Stock Status",
                "Last Updated"
        };


        inventoryTableModel =
                new DefaultTableModel(
                        inventoryColumns,
                        0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };


        inventoryTable =
                new JTable(
                        inventoryTableModel);


        JScrollPane inventoryScrollPane =
                new JScrollPane(
                        inventoryTable);


        // ==========================
        // STOCK MOVEMENT TABLE
        // ==========================

        String[] movementColumns = {

                "Movement ID",
                "Product ID",
                "Product Name",
                "Type",
                "Quantity",
                "Date",
                "Remarks"
        };


        movementTableModel =
                new DefaultTableModel(
                        movementColumns,
                        0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };


        movementTable =
                new JTable(
                        movementTableModel);


        JScrollPane movementScrollPane =
                new JScrollPane(
                        movementTable);


        // ==========================
        // DELETE MOVEMENT
        // ==========================

        deleteMovementButton =
                new JButton(
                        "Delete / Reverse Selected Movement");


        if (loggedInUser == null
                || !"ADMIN".equalsIgnoreCase(
                        loggedInUser.getRole())) {

            deleteMovementButton.setEnabled(false);
        }


        JPanel movementPanel =
                new JPanel(
                        new BorderLayout());

        movementPanel.add(
                new JLabel(
                        "Stock Movement History"),
                BorderLayout.NORTH);

        movementPanel.add(
                movementScrollPane,
                BorderLayout.CENTER);

        movementPanel.add(
                deleteMovementButton,
                BorderLayout.SOUTH);


        JPanel inventoryPanel =
                new JPanel(
                        new BorderLayout());

        inventoryPanel.add(
                new JLabel(
                        "Current Inventory"),
                BorderLayout.NORTH);

        inventoryPanel.add(
                inventoryScrollPane,
                BorderLayout.CENTER);


        JSplitPane splitPane =
                new JSplitPane(
                        JSplitPane.VERTICAL_SPLIT,
                        inventoryPanel,
                        movementPanel);

        splitPane.setDividerLocation(280);


        add(
                topPanel,
                BorderLayout.NORTH);

        add(
                splitPane,
                BorderLayout.CENTER);


        // ==========================
        // EVENTS
        // ==========================

        addStockButton.addActionListener(
                e -> addStock());

        adjustStockButton.addActionListener(
                e -> adjustStock());

        searchButton.addActionListener(
                e -> searchInventory());

        showAllButton.addActionListener(
                e -> {

                    searchField.setText("");
                    loadInventory();
                });

        clearButton.addActionListener(
                e -> clearFields());

        deleteMovementButton.addActionListener(
                e -> deleteMovement());


        productCombo.addActionListener(
                e -> updateCurrentStockLabel());


        inventoryTable
                .getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {
                                selectProductFromInventoryTable();
                            }
                        });
    }


    // ============================================
    // LOAD ACTIVE PRODUCTS
    // ============================================

    private void loadProducts() {

        productCombo.removeAllItems();

        List<Product> products =
                productDAO.getAllProducts();

        for (Product product : products) {

            if ("ACTIVE".equalsIgnoreCase(
                    product.getStatus())) {

                productCombo.addItem(
                        new ProductItem(
                                product.getProductId(),
                                product.getProductName()));
            }
        }
    }


    // ============================================
    // ADD STOCK
    // ============================================

    private void addStock() {

        ProductItem product =
                (ProductItem)
                        productCombo.getSelectedItem();

        if (product == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a product.");

            return;
        }


        int quantity;

        try {

            quantity =
                    Integer.parseInt(
                            quantityField
                                    .getText()
                                    .trim());

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Quantity must be a whole number.");

            return;
        }


        if (quantity <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Stock quantity must be greater than zero.");

            return;
        }


        boolean success =
                inventoryDAO.addStock(
                        product.getProductId(),
                        quantity,
                        remarksField
                                .getText()
                                .trim());


        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Stock added successfully!");

            clearFields();
            refreshAll();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not add stock.");
        }
    }


    // ============================================
    // ADJUST STOCK
    // ============================================

    private void adjustStock() {

        ProductItem product =
                (ProductItem)
                        productCombo.getSelectedItem();

        if (product == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a product.");

            return;
        }


        int adjustment;

        try {

            adjustment =
                    Integer.parseInt(
                            quantityField
                                    .getText()
                                    .trim());

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid adjustment quantity.");

            return;
        }


        if (adjustment == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Adjustment cannot be zero.");

            return;
        }


        boolean success =
                inventoryDAO.adjustStock(
                        product.getProductId(),
                        adjustment,
                        remarksField
                                .getText()
                                .trim());


        if (success) {

            JOptionPane.showMessageDialog(
                    this,
                    "Stock adjusted successfully!");

            clearFields();
            refreshAll();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Adjustment failed.\n"
                    + "Check that the stock will not become negative.");
        }
    }


    // ============================================
    // LOAD INVENTORY
    // ============================================

    private void loadInventory() {

        List<Inventory> inventoryList =
                inventoryDAO.getAllInventory();

        displayInventory(
                inventoryList);
    }


    // ============================================
    // SEARCH
    // ============================================

    private void searchInventory() {

        String keyword =
                searchField
                        .getText()
                        .trim();

        if (keyword.isEmpty()) {

            loadInventory();
            return;
        }


        List<Inventory> inventoryList =
                inventoryDAO
                        .searchInventory(
                                keyword);

        displayInventory(
                inventoryList);
    }


    // ============================================
    // DISPLAY INVENTORY
    // ============================================

    private void displayInventory(
            List<Inventory> inventoryList) {

        inventoryTableModel.setRowCount(0);

        int lowStockCount = 0;


        for (Inventory inventory :
                inventoryList) {

            inventoryTableModel.addRow(
                    new Object[] {

                            inventory.getInventoryId(),
                            inventory.getProductId(),
                            inventory.getProductName(),
                            inventory.getQuantity(),
                            inventory.getReorderLevel(),
                            inventory.getStockStatus(),
                            inventory.getLastUpdated()
                    });


            if ("LOW STOCK".equalsIgnoreCase(
                    inventory.getStockStatus())

                    || "OUT OF STOCK".equalsIgnoreCase(
                            inventory.getStockStatus())) {

                lowStockCount++;
            }
        }


        lowStockLabel.setText(
                "Low Stock Items: "
                + lowStockCount);


        updateCurrentStockLabel();
    }


    // ============================================
    // LOAD MOVEMENTS
    // ============================================

    private void loadStockMovements() {

        movementTableModel.setRowCount(0);


        List<StockMovement> movements =
                inventoryDAO
                        .getAllStockMovements();


        for (StockMovement movement :
                movements) {

            movementTableModel.addRow(
                    new Object[] {

                            movement.getMovementId(),
                            movement.getProductId(),
                            movement.getProductName(),
                            movement.getMovementType(),
                            movement.getQuantity(),
                            movement.getMovementDate(),
                            movement.getRemarks()
                    });
        }
    }


    // ============================================
    // DELETE / REVERSE MOVEMENT
    // ============================================

    private void deleteMovement() {

        int selectedRow =
                movementTable
                        .getSelectedRow();


        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a stock movement first.");

            return;
        }


        int movementId =
                Integer.parseInt(
                        movementTableModel
                                .getValueAt(
                                        selectedRow,
                                        0)
                                .toString());


        String movementType =
                movementTableModel
                        .getValueAt(
                                selectedRow,
                                3)
                        .toString();


        if ("SALE".equalsIgnoreCase(
                movementType)

                || "SALE_CANCEL".equalsIgnoreCase(
                        movementType)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Sales-related stock movements cannot be manually deleted.");

            return;
        }


        int answer =
                JOptionPane.showConfirmDialog(
                        this,
                        "Delete this stock movement and reverse its effect?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION);


        if (answer ==
                JOptionPane.YES_OPTION) {


            boolean success =
                    inventoryDAO
                            .deleteStockMovement(
                                    movementId);


            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Stock movement reversed successfully!");

                refreshAll();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Could not reverse this movement.\n"
                        + "The operation may result in invalid stock.");
            }
        }
    }


    // ============================================
    // SELECT PRODUCT FROM INVENTORY TABLE
    // ============================================

    private void selectProductFromInventoryTable() {

        int row =
                inventoryTable
                        .getSelectedRow();

        if (row == -1) {
            return;
        }


        int productId =
                Integer.parseInt(
                        inventoryTableModel
                                .getValueAt(
                                        row,
                                        1)
                                .toString());


        for (int i = 0;
             i < productCombo.getItemCount();
             i++) {

            ProductItem item =
                    productCombo.getItemAt(i);

            if (item.getProductId()
                    == productId) {

                productCombo
                        .setSelectedIndex(i);

                break;
            }
        }


        updateCurrentStockLabel();
    }


    // ============================================
    // CURRENT STOCK LABEL
    // ============================================

    private void updateCurrentStockLabel() {

        ProductItem selectedProduct =
                (ProductItem)
                        productCombo
                                .getSelectedItem();


        if (selectedProduct == null) {

            currentStockLabel.setText(
                    "Current Stock: -");

            return;
        }


        List<Inventory> inventoryList =
                inventoryDAO
                        .getAllInventory();


        for (Inventory inventory :
                inventoryList) {

            if (inventory.getProductId()
                    == selectedProduct
                            .getProductId()) {

                currentStockLabel.setText(
                        "Current Stock: "
                        + inventory.getQuantity());

                return;
            }
        }


        currentStockLabel.setText(
                "Current Stock: 0");
    }


    // ============================================
    // REFRESH
    // ============================================

    private void refreshAll() {

        loadInventory();
        loadStockMovements();
        updateCurrentStockLabel();
    }


    // ============================================
    // CLEAR
    // ============================================

    private void clearFields() {

        quantityField.setText("");
        remarksField.setText("");

        if (productCombo.getItemCount() > 0) {

            productCombo.setSelectedIndex(0);
        }

        inventoryTable.clearSelection();

        updateCurrentStockLabel();
    }


    // ============================================
    // PRODUCT COMBO ITEM
    // ============================================

    private static class ProductItem {

        private int productId;
        private String productName;

        public ProductItem(
                int productId,
                String productName) {

            this.productId =
                    productId;

            this.productName =
                    productName;
        }


        public int getProductId() {
            return productId;
        }


        @Override
        public String toString() {

            return productId
                    + " - "
                    + productName;
        }
    }
}