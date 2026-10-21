package model;

import interfaces.Searchable;
import interfaces.Trackable;
import java.time.LocalDate;

// Encapsulates complete member profile, implementing Searchable and Trackable
public class Member implements Searchable, Trackable {
    private String memberId;
    private String fullName;
    private String contactNumber;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String address;
    private Membership membership;
    private double membershipFee;
    private String trainerName;
    private LocalDate joiningDate;
    private LocalDate expiryDate;
    private String status; // Registered, Active, Due for Renewal, Renewed, Expired
    private Locker locker;

    public Member() {
    }

    public Member(String memberId, String fullName, String contactNumber,
                  String emergencyContactName, String emergencyContactNumber,
                  String address, Membership membership, String trainerName,
                  LocalDate joiningDate, LocalDate expiryDate, String status, Locker locker) {
        this.memberId = memberId;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = emergencyContactNumber;
        this.address = address;
        this.membership = membership;
        this.membershipFee = (membership != null) ? membership.calculateFee() : 0.0;
        this.trainerName = (trainerName == null || trainerName.trim().isEmpty()) ? "None" : trainerName.trim();
        this.joiningDate = joiningDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.locker = locker;
    }

    @Override
    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    public void setEmergencyContactNumber(String emergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Membership getMembership() {
        return membership;
    }

    public void setMembership(Membership membership) {
        this.membership = membership;
        if (membership != null) {
            this.membershipFee = membership.calculateFee();
        }
    }

    public double getMembershipFee() {
        return membershipFee;
    }

    public void setMembershipFee(double membershipFee) {
        this.membershipFee = membershipFee;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    public Locker getLocker() {
        return locker;
    }

    public void setLocker(Locker locker) {
        this.locker = locker;
    }

    // Searchable implementation: searches across key member attributes
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String lower = keyword.trim().toLowerCase();
        boolean matchMember = (memberId != null && memberId.toLowerCase().contains(lower)) ||
                              (fullName != null && fullName.toLowerCase().contains(lower)) ||
                              (contactNumber != null && contactNumber.contains(lower)) ||
                              (status != null && status.toLowerCase().contains(lower));

        boolean matchPlan = (membership != null && membership.getType().toLowerCase().contains(lower));
        boolean matchLocker = (locker != null && locker.getLocationDisplay().toLowerCase().contains(lower));

        return matchMember || matchPlan || matchLocker;
    }

    // Trackable implementation: returns a readable status overview
    @Override
    public String getTrackingSummary() {
        return "Member: " + memberId + " (" + fullName + ") | Status: " + status + " | Expiry: " + expiryDate;
    }
}