package service;

import database.MemberDAO;
import exceptions.GymException;
import exceptions.InvalidInputException;
import exceptions.MemberNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Locker;
import model.Member;
import model.Membership;
import model.Renewal;

// Service Layer: Implements business logic, validations, and delegates to MemberDAO
public class GymService {

    private final MemberDAO memberDAO;

    public GymService() {
        this.memberDAO = new MemberDAO();
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

            // Update membership plan duration if specified
            if (duration != null && !duration.trim().isEmpty()) {
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

    // Searches members by category and keyword
    public List<Member> searchMembers(String criteria, String keyword) throws GymException {
        try {
            return memberDAO.searchMembers(criteria, keyword);
        } catch (SQLException e) {
            throw new GymException("Database error during search: " + e.getMessage(), e);
        }
    }

    // Retrieves list of members who are due for renewal
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
        Member member = getMember(memberId);
        Membership m = MembershipFactory.createMembership(member.getMembership().getType(), duration);
        return m.calculateRenewalFee();
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