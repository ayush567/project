package model;

import java.time.LocalDate;

// Encapsulates renewal transaction records
public class Renewal {
    private int renewalId;
    private String memberId;
    private LocalDate renewalDate;
    private LocalDate newExpiryDate;
    private double amountPaid;
    private String paymentMode;

    public Renewal() {
    }

    public Renewal(String memberId, LocalDate renewalDate, LocalDate newExpiryDate, double amountPaid, String paymentMode) {
        this.memberId = memberId;
        this.renewalDate = renewalDate;
        this.newExpiryDate = newExpiryDate;
        this.amountPaid = amountPaid;
        this.paymentMode = paymentMode;
    }

    public Renewal(int renewalId, String memberId, LocalDate renewalDate, LocalDate newExpiryDate, double amountPaid, String paymentMode) {
        this.renewalId = renewalId;
        this.memberId = memberId;
        this.renewalDate = renewalDate;
        this.newExpiryDate = newExpiryDate;
        this.amountPaid = amountPaid;
        this.paymentMode = paymentMode;
    }

    public int getRenewalId() {
        return renewalId;
    }

    public void setRenewalId(int renewalId) {
        this.renewalId = renewalId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDate renewalDate) {
        this.renewalDate = renewalDate;
    }

    public LocalDate getNewExpiryDate() {
        return newExpiryDate;
    }

    public void setNewExpiryDate(LocalDate newExpiryDate) {
        this.newExpiryDate = newExpiryDate;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
}