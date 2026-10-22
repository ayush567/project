package database;

import exceptions.GymException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Attendance;

// Data Access Object for occasional member attendance tracking and history
public class AttendanceDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Inserts a new attendance record or updates status if one already exists for that date.
    // Does not reset the paid flag of an already paid day. Returns true if updated, false if inserted.
    public boolean saveOrUpdateAttendance(Attendance attendance) throws GymException {
        // First check if an existing record exists and whether it has been paid
        Attendance existing = getAttendance(attendance.getMemberId(), attendance.getAttendanceDate());
        if (existing != null && existing.isPaid()) {
            throw new GymException("Cannot change attendance for date " + attendance.getAttendanceDate()
                    + " as this date has already been paid.");
        }

        String sql = "INSERT INTO attendance (member_id, attendance_date, status, paid) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE status = IF(paid = 1, status, VALUES(status))";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, attendance.getMemberId());
            ps.setDate(2, Date.valueOf(attendance.getAttendanceDate()));
            ps.setString(3, attendance.getStatus());
            ps.setBoolean(4, attendance.isPaid());
            ps.executeUpdate();
            return (existing != null);
        } catch (SQLException e) {
            throw new GymException("Database error saving attendance: " + e.getMessage(), e);
        }
    }

    // Retrieves attendance record for a specific member and date
    public Attendance getAttendance(String memberId, LocalDate date) throws GymException {
        String sql = "SELECT * FROM attendance WHERE member_id = ? AND attendance_date = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new GymException("Database error looking up attendance: " + e.getMessage(), e);
        }
    }

    // Retrieves complete attendance history for a member ordered by date descending
    public List<Attendance> getAttendanceHistory(String memberId) throws GymException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE member_id = ? ORDER BY attendance_date DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new GymException("Database error fetching attendance history: " + e.getMessage(), e);
        }
    }

    // Retrieves all unpaid attendance records for a member up to a specific till date ordered by date ascending
    public List<Attendance> getUnpaidAttendanceUpTo(String memberId, LocalDate tillDate) throws GymException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE member_id = ? AND paid = 0 AND attendance_date <= ? ORDER BY attendance_date ASC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setDate(2, Date.valueOf(tillDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new GymException("Database error retrieving unpaid attendance: " + e.getMessage(), e);
        }
    }

    // Marks unpaid attendance records up to and including the till date as paid
    public int markAsPaid(String memberId, LocalDate tillDate) throws GymException {
        String sql = "UPDATE attendance SET paid = 1 WHERE member_id = ? AND attendance_date <= ? AND paid = 0";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setDate(2, Date.valueOf(tillDate));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new GymException("Database error marking attendance as paid: " + e.getMessage(), e);
        }
    }

    // Helper: Maps a ResultSet row to an Attendance model instance
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        return new Attendance(
                rs.getInt("attendance_id"),
                rs.getString("member_id"),
                rs.getDate("attendance_date").toLocalDate(),
                rs.getString("status"),
                rs.getBoolean("paid")
        );
    }
}
