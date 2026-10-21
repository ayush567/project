package model;

// Encapsulates physical locker location assigned to a gym member
public class Locker {
    private int lockerId;
    private String memberId;
    private String zone;         // e.g., Men's, Women's, Unisex
    private String rowNumber;    // e.g., Row-A, Row-1
    private String lockerNumber; // e.g., L-101, 42

    public Locker() {
    }

    public Locker(String memberId, String zone, String rowNumber, String lockerNumber) {
        this.memberId = memberId;
        this.zone = zone;
        this.rowNumber = rowNumber;
        this.lockerNumber = lockerNumber;
    }

    public Locker(int lockerId, String memberId, String zone, String rowNumber, String lockerNumber) {
        this.lockerId = lockerId;
        this.memberId = memberId;
        this.zone = zone;
        this.rowNumber = rowNumber;
        this.lockerNumber = lockerNumber;
    }

    public int getLockerId() {
        return lockerId;
    }

    public void setLockerId(int lockerId) {
        this.lockerId = lockerId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getLockerNumber() {
        return lockerNumber;
    }

    public void setLockerNumber(String lockerNumber) {
        this.lockerNumber = lockerNumber;
    }

    public String getLocationDisplay() {
        return zone + " | " + rowNumber + " | No. " + lockerNumber;
    }

    @Override
    public String toString() {
        return getLocationDisplay();
    }
}