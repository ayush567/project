package service;

import database.AttendanceDAO;
import database.MemberDAO;
import exceptions.GymException;
import exceptions.InvalidInputException;
import exceptions.MemberNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Attendance;
import model.AttendanceFeeSummary;
import model.Locker;
import model.Member;
import model.Membership;
import model.Renewal;

// Service Layer: Implements business logic, validations, and delegates to MemberDAO and AttendanceDAO
public class GymService {

    private final MemberDAO memberDAO;
    private final AttendanceDAO attendanceDAO;

    public GymService() {
        this.memberDAO = new MemberDAO();
        this.attendanceDAO = new AttendanceDAO();
    }

    // Validates inputs and registers a new member
    public void registerMember(Member member) throws GymException {
        if (member == null) {
            throw new InvalidInputException("Member details cannot be null.");
        }
        validateMemberFields(member);

        try {
            if (memberDAO.isMemberIdExists(member.getMemberId())) {
                throw new InvalidInputException("Member ID '" + member.getMemberId() + "' already exists.");
            }
            memberDAO.addMember(member);
        } catch (SQLException e) {
            throw new GymException("Database error registering member: " + e.getMessage(), e);
        }
    }

    // Updates an existing member profile
    public void updateMember(Member member) throws GymException {
        if (member == null || member.getMemberId() == null) {
            throw new InvalidInputException("Valid member details required for update.");
        }
        validateMemberFields(member);

        try {
            if (!memberDAO.isMemberIdExists(member.getMemberId())) {
                throw new MemberNotFoundException("Member with ID '" + member.getMemberId() + "' not found.");
            }
            memberDAO.updateMember(member);
        } catch (SQLException e) {
            throw new GymException("Database error updating member: " + e.getMessage(), e);
        }
    }

    // Updates locker location for a member
    public void updateLocker(String memberId, String zone, String row, String lockerNo) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Member ID cannot be empty.");
        }
        if (zone == null || zone.trim().isEmpty() || row == null || row.trim().isEmpty() || lockerNo == null || lockerNo.trim().isEmpty()) {
            throw new InvalidInputException("Zone, row, and locker number must all be provided.");
        }

        try {
            if (!memberDAO.isMemberIdExists(memberId.trim())) {
                throw new MemberNotFoundException("Member with ID '" + memberId + "' does not exist.");
            }
            Locker locker = new Locker(memberId.trim(), zone.trim(), row.trim(), lockerNo.trim());
            memberDAO.updateLocker(memberId.trim(), locker);
        } catch (SQLException e) {
            throw new GymException("Database error updating locker: " + e.getMessage(), e);
        }
    }

    // Updates member status with validation against allowed values
    public void updateMemberStatus(String memberId, String newStatus) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Member ID cannot be empty.");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new InvalidInputException("Status cannot be empty.");
        }

        String validStatus = newStatus.trim();
        if (!validStatus.equals("Registered") && !validStatus.equals("Active") &&
            !validStatus.equals("Due for Renewal") && !validStatus.equals("Renewed") &&
            !validStatus.equals("Expired")) {
            throw new InvalidInputException("Invalid status: " + validStatus +
                    ". Allowed: Registered, Active, Due for Renewal, Renewed, Expired.");
        }

        try {
            if (!memberDAO.isMemberIdExists(memberId.trim())) {
                throw new MemberNotFoundException("Member with ID '" + memberId + "' does not exist.");
            }
            memberDAO.updateStatus(memberId.trim(), validStatus);
        } catch (SQLException e) {
            throw new GymException("Database error updating status: " + e.getMessage(), e);
        }
    }

    // Processes a membership renewal with polymorphic discounted fee calculation
    public void renewMembership(String memberId, String duration, double amountPaid, String paymentMode) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Member ID cannot be empty.");
        }
        if (amountPaid <= 0) {
            throw new InvalidInputException("Renewal payment amount must be positive.");
        }
        if (paymentMode == null || paymentMode.trim().isEmpty()) {
            throw new InvalidInputException("Payment mode cannot be empty.");
        }

        try {
            Member member = memberDAO.getMemberById(memberId.trim());
            if (member == null) {
                throw new MemberNotFoundException("Member with ID '" + memberId + "' not found.");
            }

            if (member.isOccasional()) {
                throw new InvalidInputException("Occasional members do not have a renewal cycle.");
            }

            // Update membership plan duration if specified
            if (duration != null && !duration.trim().isEmpty()) {
                if ("Occasional".equalsIgnoreCase(duration.trim())) {
                    throw new InvalidInputException("Cannot renew a fixed membership into an Occasional plan via renewal.");
                }
                member.getMembership().setDuration(duration.trim());
            }

            int monthsToAdd = member.getMembership().getDurationMonths();

            // Calculate new expiry date based on current expiry or today
            LocalDate baseDate = member.getExpiryDate();
            if (baseDate == null || baseDate.isBefore(LocalDate.now())) {
                baseDate = LocalDate.now();
            }
            LocalDate newExpiryDate = baseDate.plusMonths(monthsToAdd);

            Renewal renewal = new Renewal(
                    member.getMemberId(),
                    LocalDate.now(),
                    newExpiryDate,
                    amountPaid,
                    paymentMode.trim()
            );

            memberDAO.recordRenewal(renewal);
        } catch (SQLException e) {
            throw new GymException("Database error processing renewal: " + e.getMessage(), e);
        }
    }

    // Deletes member by ID
    public void deleteMember(String memberId) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Member ID cannot be empty.");
        }
        try {
            if (!memberDAO.isMemberIdExists(memberId.trim())) {
                throw new MemberNotFoundException("Member with ID '" + memberId + "' does not exist.");
            }
            memberDAO.deleteMember(memberId.trim());
        } catch (SQLException e) {
            throw new GymException("Database error deleting member: " + e.getMessage(), e);
        }
    }

    // Retrieves member by ID
    public Member getMember(String memberId) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Member ID cannot be empty.");
        }
        try {
            Member member = memberDAO.getMemberById(memberId.trim());
            if (member == null) {
                throw new MemberNotFoundException("Member with ID '" + memberId + "' not found.");
            }
            return member;
        } catch (SQLException e) {
            throw new GymException("Database error fetching member: " + e.getMessage(), e);
        }
    }

    // Retrieves all members
    public List<Member> getAllMembers() throws GymException {
        try {
            return memberDAO.getAllMembers();
        } catch (SQLException e) {
            throw new GymException("Database error retrieving members: " + e.getMessage(), e);
        }
    }

    // Retrieves all occasional members
    public List<Member> getOccasionalMembers() throws GymException {
        try {
            return memberDAO.getOccasionalMembers();
        } catch (SQLException e) {
            throw new GymException("Database error retrieving occasional members: " + e.getMessage(), e);
        }
    }

    // Searches members by category and keyword
    public List<Member> searchMembers(String criteria, String keyword) throws GymException {
        try {
            return memberDAO.searchMembers(criteria, keyword);
        } catch (SQLException e) {
            throw new GymException("Database error during search: " + e.getMessage(), e);
        }
    }

    // Retrieves list of members who are due for renewal (excludes occasional members)
    public List<Member> getMembersDueForRenewal() throws GymException {
        try {
            return memberDAO.getMembersDueForRenewal();
        } catch (SQLException e) {
            throw new GymException("Database error fetching due renewals: " + e.getMessage(), e);
        }
    }

    // Retrieves renewal history for a member
    public List<Renewal> getRenewals(String memberId) throws GymException {
        try {
            return memberDAO.getRenewalsByMemberId(memberId.trim());
        } catch (SQLException e) {
            throw new GymException("Database error fetching renewals: " + e.getMessage(), e);
        }
    }

    // Helper: Generates next sequential member ID
    public String generateNextMemberId() throws GymException {
        try {
            return memberDAO.getNextMemberId();
        } catch (SQLException e) {
            throw new GymException("Database error generating ID: " + e.getMessage(), e);
        }
    }

    // Calculates standard registration fee for plan and duration
    public double calculateFee(String type, String duration) throws InvalidInputException {
        Membership m = MembershipFactory.createMembership(type, duration);
        return m.calculateFee();
    }

    // Calculates renewal fee applying the plan's polymorphic discount
    public double calculateRenewalFee(String memberId, String duration) throws GymException {
        if ("Occasional".equalsIgnoreCase(duration)) {
            throw new InvalidInputException("Occasional plans do not have a renewal fee.");
        }
        Member member = getMember(memberId);
        if (member.isOccasional()) {
            throw new InvalidInputException("Occasional members do not have a renewal cycle.");
        }
        Membership m = MembershipFactory.createMembership(member.getMembership().getType(), duration);
        return m.calculateRenewalFee();
    }

    // =========================================================================
    // ATTENDANCE & ON-DEMAND FEE CALCULATION BUSINESS LOGIC (OCCASIONAL MEMBERS)
    // =========================================================================

    // Marks attendance for an occasional member. If a record already exists, updates it.
    // Returns true if an existing record was updated, false if a new record was inserted.
    public boolean markAttendance(String memberId, LocalDate date, String status) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Please select an occasional member.");
        }
        Member member = getMember(memberId.trim());
        if (!member.isOccasional()) {
            throw new InvalidInputException("Attendance can only be marked for members with 'Occasional' plan duration.");
        }
        if (date == null) {
            throw new InvalidInputException("Attendance date is required.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidInputException("Attendance cannot be marked for a future date: " + date);
        }
        if (status == null || (!status.equalsIgnoreCase("Present") && !status.equalsIgnoreCase("Absent"))) {
            throw new InvalidInputException("Attendance status must be 'Present' or 'Absent'.");
        }

        String normalizedStatus = status.equalsIgnoreCase("Present") ? "Present" : "Absent";
        Attendance attendance = new Attendance(member.getMemberId(), date, normalizedStatus, false);
        return attendanceDAO.saveOrUpdateAttendance(attendance);
    }

    // Retrieves complete attendance history for a member
    public List<Attendance> getMemberAttendanceHistory(String memberId) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Member ID is required.");
        }
        return attendanceDAO.getAttendanceHistory(memberId.trim());
    }

    // Calculates fee till date for unpaid Present days of an occasional member
    public AttendanceFeeSummary calculateFeeTillDate(String memberId, LocalDate tillDate) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Please select an occasional member.");
        }
        Member member = getMember(memberId.trim());
        if (!member.isOccasional()) {
            throw new InvalidInputException("Fee till date can only be calculated for Occasional members.");
        }
        if (tillDate == null) {
            throw new InvalidInputException("Till date is required.");
        }
        if (tillDate.isAfter(LocalDate.now())) {
            throw new InvalidInputException("Till date cannot be in the future: " + tillDate);
        }

        List<Attendance> unpaidList = attendanceDAO.getUnpaidAttendanceUpTo(member.getMemberId(), tillDate);

        List<LocalDate> presentDates = new ArrayList<>();
        int absentCount = 0;
        LocalDate earliestUnpaidDate = null;

        for (Attendance att : unpaidList) {
            if (earliestUnpaidDate == null) {
                earliestUnpaidDate = att.getAttendanceDate();
            }
            if ("Present".equalsIgnoreCase(att.getStatus())) {
                presentDates.add(att.getAttendanceDate());
            } else if ("Absent".equalsIgnoreCase(att.getStatus())) {
                absentCount++;
            }
        }

        if (presentDates.isEmpty()) {
            return null; // Signals nothing payable
        }

        double dailyRate = member.getMembership().getDailyRate();
        double totalAmount = presentDates.size() * dailyRate;

        return new AttendanceFeeSummary(
                member.getMemberId(),
                member.getFullName(),
                member.getMembership().getType(),
                earliestUnpaidDate,
                tillDate,
                presentDates,
                presentDates.size(),
                absentCount,
                dailyRate,
                totalAmount
        );
    }

    // Marks unpaid attendance records up to till date as paid
    public void markAttendanceAsPaid(String memberId, LocalDate tillDate) throws GymException {
        if (memberId == null || memberId.trim().isEmpty()) {
            throw new InvalidInputException("Member ID is required.");
        }
        Member member = getMember(memberId.trim());
        if (!member.isOccasional()) {
            throw new InvalidInputException("Payment can only be marked for Occasional members.");
        }
        if (tillDate == null) {
            throw new InvalidInputException("Till date is required.");
        }
        if (tillDate.isAfter(LocalDate.now())) {
            throw new InvalidInputException("Till date cannot be in the future: " + tillDate);
        }

        attendanceDAO.markAsPaid(member.getMemberId(), tillDate);
    }

    // Validates basic member attributes
    private void validateMemberFields(Member member) throws InvalidInputException {
        if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
            throw new InvalidInputException("Member ID is required.");
        }
        if (member.getFullName() == null || member.getFullName().trim().isEmpty()) {
            throw new InvalidInputException("Member full name is required.");
        }
        if (member.getContactNumber() == null || member.getContactNumber().trim().length() < 7) {
            throw new InvalidInputException("Valid member contact number is required.");
        }
        if (member.getEmergencyContactName() == null || member.getEmergencyContactName().trim().isEmpty()) {
            throw new InvalidInputException("Emergency contact name is required.");
        }
        if (member.getEmergencyContactNumber() == null || member.getEmergencyContactNumber().trim().length() < 7) {
            throw new InvalidInputException("Valid emergency contact number is required.");
        }
        if (member.getAddress() == null || member.getAddress().trim().isEmpty()) {
            throw new InvalidInputException("Member address is required.");
        }
        if (member.getMembership() == null) {
            throw new InvalidInputException("Membership plan must be assigned.");
        }
    }
}
