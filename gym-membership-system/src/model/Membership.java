package model;

import interfaces.Renewable;

// Abstract base class representing a generic gym membership plan
public abstract class Membership implements Renewable {
    private String type;
    private String duration; // Monthly, Quarterly, Yearly
    private double baseMonthlyRate;

    public Membership(String type, String duration, double baseMonthlyRate) {
        this.type = type;
        this.duration = duration;
        this.baseMonthlyRate = baseMonthlyRate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getBaseMonthlyRate() {
        return baseMonthlyRate;
    }

    public void setBaseMonthlyRate(double baseMonthlyRate) {
        this.baseMonthlyRate = baseMonthlyRate;
    }

    // Helper method to convert plan duration into number of months
    public int getDurationMonths() {
        if ("Quarterly".equalsIgnoreCase(duration)) {
            return 3;
        } else if ("Yearly".equalsIgnoreCase(duration)) {
            return 12;
        }
        return 1; // Default is Monthly
    }

    // Abstract method overridden by subclasses (Polymorphism)
    @Override
    public abstract double calculateFee();

    // Abstract method overridden by subclasses for renewal discount
    @Override
    public abstract double getRenewalDiscountPercentage();

    // Calculates renewal fee after applying the specific membership discount
    @Override
    public double calculateRenewalFee() {
        double regularFee = calculateFee();
        double discountAmount = regularFee * (getRenewalDiscountPercentage() / 100.0);
        return regularFee - discountAmount;
    }
}