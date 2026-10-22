package model;

import java.time.LocalDate;

// Model class representing a single attendance record for an occasional member
public class Attendance {
    private int attendanceId;
    private String memberId;
    private LocalDate attendanceDate;
    private String status; // 'Present' or 'Absent'
    private boolean paid;

    public Attendance() {
    }

    public Attendance(String memberId, LocalDate attendanceDate, String status, boolean paid) {
        this.memberId = memberId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.paid = paid;
    }

    public Attendance(int attendanceId, String memberId, LocalDate attendanceDate, String status, boolean paid) {
        this.attendanceId = attendanceId;
        this.memberId = memberId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.paid = paid;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return "Attendance [id=" + attendanceId + ", memberId=" + memberId + ", date=" + attendanceDate
                + ", status=" + status + ", paid=" + paid + "]";
    }
}
