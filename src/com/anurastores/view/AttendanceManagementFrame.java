package com.anurastores.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.anurastores.dao.AttendanceDAO;
import com.anurastores.dao.EmployeeDAO;
import com.anurastores.model.Attendance;
import com.anurastores.model.Employee;
import com.anurastores.model.User;

public class AttendanceManagementFrame extends JFrame {

    private JTextField attendanceIdField;
    private JComboBox<EmployeeItem> employeeCombo;
    private JTextField dateField;
    private JTextField checkInField;
    private JTextField checkOutField;
    private JComboBox<String> statusCombo;
    private JTextField remarksField;

    private JTextField searchField;
    private JTextField fromDateField;
    private JTextField toDateField;

    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    private AttendanceDAO attendanceDAO;
    private EmployeeDAO employeeDAO;

    private User loggedInUser;
    private JButton deleteButton;

    public AttendanceManagementFrame(User user) {

        this.loggedInUser = user;

        attendanceDAO = new AttendanceDAO();
        employeeDAO = new EmployeeDAO();

        setTitle("Attendance Management - Anura Stores");
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        loadEmployees();
        loadAttendance();
    }

    private void initComponents() {

        // ---------------- FORM ----------------

        JPanel formPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        attendanceIdField = new JTextField(15);
        attendanceIdField.setEditable(false);

        employeeCombo = new JComboBox<EmployeeItem>();

        dateField = new JTextField(15);
        checkInField = new JTextField(15);
        checkOutField = new JTextField(15);

        statusCombo = new JComboBox<String>(
                new String[] {
                        "PRESENT",
                        "ABSENT",
                        "LATE",
                        "LEAVE"
                });

        remarksField = new JTextField(15);

        addRow(formPanel, gbc, 0,
                "Attendance ID:", attendanceIdField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Employee:"), gbc);

        gbc.gridx = 1;
        formPanel.add(employeeCombo, gbc);

        addRow(formPanel, gbc, 2,
                "Date (YYYY-MM-DD):", dateField);

        addRow(formPanel, gbc, 3,
                "Check In (HH:MM):", checkInField);

        addRow(formPanel, gbc, 4,
                "Check Out (HH:MM):", checkOutField);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        formPanel.add(statusCombo, gbc);

        addRow(formPanel, gbc, 6,
                "Remarks:", remarksField);


        // ---------------- BUTTONS ----------------

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");

        JPanel buttonPanel =
                new JPanel(new GridLayout(1, 4, 10, 10));

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Only ADMIN can delete attendance
        if (loggedInUser == null
                || !"ADMIN".equalsIgnoreCase(
                        loggedInUser.getRole())) {

            deleteButton.setEnabled(false);
        }


        // ---------------- SEARCH ----------------

        searchField = new JTextField(15);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        fromDateField = new JTextField(10);
        toDateField = new JTextField(10);

        JButton dateRangeButton =
                new JButton("Filter Date Range");

        JPanel searchPanel = new JPanel();

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        searchPanel.add(new JLabel("From:"));
        searchPanel.add(fromDateField);

        searchPanel.add(new JLabel("To:"));
        searchPanel.add(toDateField);

        searchPanel.add(dateRangeButton);
        searchPanel.add(showAllButton);


        // ---------------- TABLE ----------------

        String[] columns = {
                "Attendance ID",
                "Employee ID",
                "Employee Name",
                "Date",
                "Check In",
                "Check Out",
                "Status",
                "Remarks"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        attendanceTable = new JTable(tableModel);

        JScrollPane scrollPane =
                new JScrollPane(attendanceTable);


        // ---------------- LAYOUT ----------------

        JPanel leftPanel =
                new JPanel(new BorderLayout());

        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(searchPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);


        // ---------------- EVENTS ----------------

        addButton.addActionListener(
                e -> addAttendance());

        updateButton.addActionListener(
                e -> updateAttendance());

        deleteButton.addActionListener(
                e -> deleteAttendance());

        clearButton.addActionListener(
                e -> clearFields());

        searchButton.addActionListener(
                e -> searchAttendance());

        dateRangeButton.addActionListener(
                e -> filterDateRange());

        showAllButton.addActionListener(e -> {

            searchField.setText("");
            fromDateField.setText("");
            toDateField.setText("");

            loadAttendance();
        });

        attendanceTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        fillFormFromTable();
                    }
                });
    }


    private void addRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JTextField field) {

        gbc.gridx = 0;
        gbc.gridy = row;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;

        panel.add(field, gbc);
    }


    // ===================================================
    // LOAD ACTIVE EMPLOYEES
    // ===================================================

    private void loadEmployees() {

        employeeCombo.removeAllItems();

        List<Employee> employees =
                employeeDAO.getAllEmployees();

        for (Employee employee : employees) {

            if ("ACTIVE".equalsIgnoreCase(
                    employee.getStatus())) {

                employeeCombo.addItem(
                        new EmployeeItem(
                                employee.getEmployeeId(),
                                employee.getFirstName()
                                + " "
                                + employee.getLastName()
                        )
                );
            }
        }
    }


    // ===================================================
    // ADD ATTENDANCE
    // ===================================================

    private void addAttendance() {

        try {

            Attendance attendance =
                    getAttendanceFromForm();

            if (attendance == null) {
                return;
            }

            boolean duplicate =
                    attendanceDAO.attendanceExists(
                            attendance.getEmployeeId(),
                            attendance.getAttendanceDate(),
                            0);

            if (duplicate) {

                JOptionPane.showMessageDialog(
                        this,
                        "Attendance already exists for this employee on this date.",
                        "Duplicate Attendance",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            boolean success =
                    attendanceDAO.addAttendance(attendance);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Attendance added successfully!");

                clearFields();
                loadAttendance();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Could not add attendance.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please check the date and time values.",
                    "Invalid Data",
                    JOptionPane.WARNING_MESSAGE);
        }
    }


    // ===================================================
    // UPDATE ATTENDANCE
    // ===================================================

    private void updateAttendance() {

        if (attendanceIdField.getText().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an attendance record first.");

            return;
        }

        try {

            Attendance attendance =
                    getAttendanceFromForm();

            if (attendance == null) {
                return;
            }

            int attendanceId =
                    Integer.parseInt(
                            attendanceIdField.getText());

            attendance.setAttendanceId(attendanceId);

            boolean duplicate =
                    attendanceDAO.attendanceExists(
                            attendance.getEmployeeId(),
                            attendance.getAttendanceDate(),
                            attendanceId);

            if (duplicate) {

                JOptionPane.showMessageDialog(
                        this,
                        "Another attendance record already exists "
                        + "for this employee on this date.",
                        "Duplicate Attendance",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            boolean success =
                    attendanceDAO.updateAttendance(attendance);

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Attendance updated successfully!");

                clearFields();
                loadAttendance();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Attendance update failed.");
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please check the date and time values.",
                    "Invalid Data",
                    JOptionPane.WARNING_MESSAGE);
        }
    }


    // ===================================================
    // DELETE ATTENDANCE
    // ===================================================

    private void deleteAttendance() {

        if (attendanceIdField.getText().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an attendance record first.");

            return;
        }

        int answer =
                JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to delete this attendance record?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {

            int attendanceId =
                    Integer.parseInt(
                            attendanceIdField.getText());

            if (attendanceDAO.deleteAttendance(
                    attendanceId)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Attendance deleted successfully!");

                clearFields();
                loadAttendance();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Could not delete attendance.");
            }
        }
    }


    // ===================================================
    // GET FORM DATA
    // ===================================================

    private Attendance getAttendanceFromForm() {

        EmployeeItem employee =
                (EmployeeItem)
                        employeeCombo.getSelectedItem();

        if (employee == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an employee.");

            return null;
        }

        if (dateField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance date is required.");

            return null;
        }

        Date attendanceDate =
                Date.valueOf(
                        dateField.getText().trim());

        Time checkIn =
                parseTime(
                        checkInField.getText().trim());

        Time checkOut =
                parseTime(
                        checkOutField.getText().trim());

        // Prevent invalid time
        if (checkIn != null
                && checkOut != null
                && checkOut.before(checkIn)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Check-out time cannot be before check-in time.",
                    "Invalid Time",
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        Attendance attendance =
                new Attendance();

        attendance.setEmployeeId(
                employee.getEmployeeId());

        attendance.setAttendanceDate(
                attendanceDate);

        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);

        attendance.setAttendanceStatus(
                statusCombo.getSelectedItem().toString());

        attendance.setRemarks(
                remarksField.getText().trim());

        return attendance;
    }


    // Accept both 08:00 and 08:00:00
    private Time parseTime(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        value = value.trim();

        if (value.length() == 5) {
            value = value + ":00";
        }

        return Time.valueOf(value);
    }


    // ===================================================
    // LOAD ALL
    // ===================================================

    private void loadAttendance() {

        displayAttendance(
                attendanceDAO.getAllAttendance());
    }


    // ===================================================
    // SEARCH
    // ===================================================

    private void searchAttendance() {

        String keyword =
                searchField.getText().trim();

        if (keyword.isEmpty()) {

            loadAttendance();
            return;
        }

        displayAttendance(
                attendanceDAO.searchAttendance(keyword));
    }


    // ===================================================
    // DATE RANGE
    // ===================================================

    private void filterDateRange() {

        try {

            if (fromDateField.getText().trim().isEmpty()
                    || toDateField.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter both From and To dates.");

                return;
            }

            Date fromDate =
                    Date.valueOf(
                            fromDateField.getText().trim());

            Date toDate =
                    Date.valueOf(
                            toDateField.getText().trim());

            if (toDate.before(fromDate)) {

                JOptionPane.showMessageDialog(
                        this,
                        "To date cannot be before From date.");

                return;
            }

            displayAttendance(
                    attendanceDAO.searchByDateRange(
                            fromDate,
                            toDate));

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Dates must be in YYYY-MM-DD format.");
        }
    }


    // ===================================================
    // DISPLAY
    // ===================================================

    private void displayAttendance(
            List<Attendance> attendanceList) {

        tableModel.setRowCount(0);

        for (Attendance attendance :
                attendanceList) {

            tableModel.addRow(new Object[] {

                    attendance.getAttendanceId(),
                    attendance.getEmployeeId(),
                    attendance.getEmployeeName(),
                    attendance.getAttendanceDate(),
                    attendance.getCheckIn(),
                    attendance.getCheckOut(),
                    attendance.getAttendanceStatus(),
                    attendance.getRemarks()
            });
        }
    }


    // ===================================================
    // TABLE ROW -> FORM
    // ===================================================

    private void fillFormFromTable() {

        int row =
                attendanceTable.getSelectedRow();

        if (row == -1) {
            return;
        }

        attendanceIdField.setText(
                getTableValue(row, 0));

        int employeeId =
                Integer.parseInt(
                        getTableValue(row, 1));

        selectEmployee(employeeId);

        dateField.setText(
                getTableValue(row, 3));

        checkInField.setText(
                getTableValue(row, 4));

        checkOutField.setText(
                getTableValue(row, 5));

        statusCombo.setSelectedItem(
                getTableValue(row, 6));

        remarksField.setText(
                getTableValue(row, 7));
    }


    private void selectEmployee(int employeeId) {

        for (int i = 0;
             i < employeeCombo.getItemCount();
             i++) {

            EmployeeItem item =
                    employeeCombo.getItemAt(i);

            if (item.getEmployeeId()
                    == employeeId) {

                employeeCombo.setSelectedIndex(i);
                return;
            }
        }
    }


    private String getTableValue(
            int row,
            int column) {

        Object value =
                tableModel.getValueAt(row, column);

        return value == null
                ? ""
                : value.toString();
    }


    // ===================================================
    // CLEAR
    // ===================================================

    private void clearFields() {

        attendanceIdField.setText("");

        if (employeeCombo.getItemCount() > 0) {
            employeeCombo.setSelectedIndex(0);
        }

        dateField.setText("");
        checkInField.setText("");
        checkOutField.setText("");
        remarksField.setText("");

        statusCombo.setSelectedItem("PRESENT");

        attendanceTable.clearSelection();
    }


    // ===================================================
    // EMPLOYEE COMBO ITEM
    // ===================================================

    private static class EmployeeItem {

        private int employeeId;
        private String employeeName;

        public EmployeeItem(
                int employeeId,
                String employeeName) {

            this.employeeId = employeeId;
            this.employeeName = employeeName;
        }

        public int getEmployeeId() {
            return employeeId;
        }

        @Override
        public String toString() {

            return employeeId
                    + " - "
                    + employeeName;
        }
    }
}