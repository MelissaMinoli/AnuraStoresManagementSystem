package com.anurastores.dao;

import java.util.List;

import com.anurastores.model.Attendance;

public class AttendanceDAOTest {

    public static void main(String[] args) {

        AttendanceDAO attendanceDAO =
                new AttendanceDAO();

        List<Attendance> attendanceList =
                attendanceDAO.getAllAttendance();

        for (Attendance attendance : attendanceList) {

            System.out.println(
                    attendance.getAttendanceId()
                    + " | "
                    + attendance.getEmployeeId()
                    + " | "
                    + attendance.getEmployeeName()
                    + " | "
                    + attendance.getAttendanceDate()
                    + " | "
                    + attendance.getAttendanceStatus()
            );
        }
    }
}