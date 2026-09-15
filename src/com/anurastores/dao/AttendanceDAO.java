package com.anurastores.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.anurastores.model.Attendance;
import com.anurastores.util.DBConnection;

public class AttendanceDAO {

    // ADD ATTENDANCE
    public boolean addAttendance(Attendance attendance) {

        String sql = "INSERT INTO attendance "
                + "(employee_id, attendance_date, check_in, check_out, "
                + "attendance_status, remarks) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, attendance.getEmployeeId());
            statement.setDate(2, attendance.getAttendanceDate());

            if (attendance.getCheckIn() != null) {
                statement.setTime(3, attendance.getCheckIn());
            } else {
                statement.setNull(3, Types.TIME);
            }

            if (attendance.getCheckOut() != null) {
                statement.setTime(4, attendance.getCheckOut());
            } else {
                statement.setNull(4, Types.TIME);
            }

            statement.setString(
                    5,
                    attendance.getAttendanceStatus()
            );

            statement.setString(
                    6,
                    attendance.getRemarks()
            );

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // VIEW ALL ATTENDANCE
    public List<Attendance> getAllAttendance() {

        List<Attendance> attendanceList =
                new ArrayList<Attendance>();

        String sql =
                "SELECT a.attendance_id, "
                + "a.employee_id, "
                + "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, "
                + "a.attendance_date, "
                + "a.check_in, "
                + "a.check_out, "
                + "a.attendance_status, "
                + "a.remarks "
                + "FROM attendance a "
                + "INNER JOIN employee e "
                + "ON a.employee_id = e.employee_id "
                + "ORDER BY a.attendance_date DESC, "
                + "a.attendance_id DESC";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {

                attendanceList.add(
                        createAttendanceFromResult(result)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return attendanceList;
    }


    // SEARCH BY EMPLOYEE ID / NAME / DATE / STATUS
    public List<Attendance> searchAttendance(String keyword) {

        List<Attendance> attendanceList =
                new ArrayList<Attendance>();

        String sql =
                "SELECT a.attendance_id, "
                + "a.employee_id, "
                + "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, "
                + "a.attendance_date, "
                + "a.check_in, "
                + "a.check_out, "
                + "a.attendance_status, "
                + "a.remarks "
                + "FROM attendance a "
                + "INNER JOIN employee e "
                + "ON a.employee_id = e.employee_id "
                + "WHERE CAST(a.attendance_id AS CHAR) LIKE ? "
                + "OR CAST(a.employee_id AS CHAR) LIKE ? "
                + "OR e.first_name LIKE ? "
                + "OR e.last_name LIKE ? "
                + "OR CONCAT(e.first_name, ' ', e.last_name) LIKE ? "
                + "OR CAST(a.attendance_date AS CHAR) LIKE ? "
                + "OR a.attendance_status LIKE ? "
                + "ORDER BY a.attendance_date DESC, "
                + "a.attendance_id DESC";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            String searchValue = "%" + keyword + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            statement.setString(4, searchValue);
            statement.setString(5, searchValue);
            statement.setString(6, searchValue);
            statement.setString(7, searchValue);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    attendanceList.add(
                            createAttendanceFromResult(result)
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return attendanceList;
    }


    // SEARCH BY DATE RANGE
    public List<Attendance> searchByDateRange(
            Date fromDate,
            Date toDate) {

        List<Attendance> attendanceList =
                new ArrayList<Attendance>();

        String sql =
                "SELECT a.attendance_id, "
                + "a.employee_id, "
                + "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, "
                + "a.attendance_date, "
                + "a.check_in, "
                + "a.check_out, "
                + "a.attendance_status, "
                + "a.remarks "
                + "FROM attendance a "
                + "INNER JOIN employee e "
                + "ON a.employee_id = e.employee_id "
                + "WHERE a.attendance_date BETWEEN ? AND ? "
                + "ORDER BY a.attendance_date DESC, "
                + "a.attendance_id DESC";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setDate(1, fromDate);
            statement.setDate(2, toDate);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    attendanceList.add(
                            createAttendanceFromResult(result)
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return attendanceList;
    }


    // UPDATE ATTENDANCE
    public boolean updateAttendance(Attendance attendance) {

        String sql =
                "UPDATE attendance SET "
                + "employee_id = ?, "
                + "attendance_date = ?, "
                + "check_in = ?, "
                + "check_out = ?, "
                + "attendance_status = ?, "
                + "remarks = ? "
                + "WHERE attendance_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    attendance.getEmployeeId()
            );

            statement.setDate(
                    2,
                    attendance.getAttendanceDate()
            );

            if (attendance.getCheckIn() != null) {

                statement.setTime(
                        3,
                        attendance.getCheckIn()
                );

            } else {

                statement.setNull(
                        3,
                        Types.TIME
                );
            }

            if (attendance.getCheckOut() != null) {

                statement.setTime(
                        4,
                        attendance.getCheckOut()
                );

            } else {

                statement.setNull(
                        4,
                        Types.TIME
                );
            }

            statement.setString(
                    5,
                    attendance.getAttendanceStatus()
            );

            statement.setString(
                    6,
                    attendance.getRemarks()
            );

            statement.setInt(
                    7,
                    attendance.getAttendanceId()
            );

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // DELETE ATTENDANCE
    public boolean deleteAttendance(int attendanceId) {

        String sql =
                "DELETE FROM attendance "
                + "WHERE attendance_id = ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, attendanceId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // CHECK DUPLICATE ATTENDANCE
    public boolean attendanceExists(
            int employeeId,
            Date attendanceDate,
            int excludeAttendanceId) {

        String sql =
                "SELECT attendance_id "
                + "FROM attendance "
                + "WHERE employee_id = ? "
                + "AND attendance_date = ? "
                + "AND attendance_id <> ?";

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, employeeId);
            statement.setDate(2, attendanceDate);
            statement.setInt(3, excludeAttendanceId);

            try (ResultSet result = statement.executeQuery()) {

                return result.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // CONVERT RESULTSET TO ATTENDANCE OBJECT
    private Attendance createAttendanceFromResult(
            ResultSet result) throws Exception {

        return new Attendance(
                result.getInt("attendance_id"),
                result.getInt("employee_id"),
                result.getString("employee_name"),
                result.getDate("attendance_date"),
                result.getTime("check_in"),
                result.getTime("check_out"),
                result.getString("attendance_status"),
                result.getString("remarks")
        );
    }
}