package model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

// Encapsulates on-demand fee calculation results for an occasional member
public class AttendanceFeeSummary {
    private final String memberId;
    private final String memberName;
    private final String planType;
    private final LocalDate fromDate;
    private final LocalDate tillDate;
    private final List<LocalDate> presentDates;
    private final int presentDaysCount;
    private final int absentDaysCount;
    private final double dailyRate;
    private final double totalAmount;

    public AttendanceFeeSummary(String memberId, String memberName, String planType,
                                LocalDate fromDate, LocalDate tillDate,
                                List<LocalDate> presentDates, int presentDaysCount,
                                int absentDaysCount, double dailyRate, double totalAmount) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.planType = planType;
        this.fromDate = fromDate;
        this.tillDate = tillDate;
        this.presentDates = (presentDates != null) ? Collections.unmodifiableList(presentDates) : Collections.emptyList();
        this.presentDaysCount = presentDaysCount;
        this.absentDaysCount = absentDaysCount;
        this.dailyRate = dailyRate;
        this.totalAmount = totalAmount;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getPlanType() {
        return planType;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getTillDate() {
        return tillDate;
    }

    public List<LocalDate> getPresentDates() {
        return presentDates;
    }

    public int getPresentDaysCount() {
        return presentDaysCount;
    }

    public int getAbsentDaysCount() {
        return absentDaysCount;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
