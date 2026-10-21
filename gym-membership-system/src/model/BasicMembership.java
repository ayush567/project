package model;

// Basic membership offering standard gym access
public class BasicMembership extends Membership {

    public BasicMembership(String duration) {
        super("Basic", duration, 1000.0);
    }

    // Standard fee calculation: Monthly: 1000, Quarterly: 5% off, Yearly: 15% off
    @Override
    public double calculateFee() {
        int months = getDurationMonths();
        double total = getBaseMonthlyRate() * months;
        if (months == 3) {
            return total * 0.95; // 5% discount
        } else if (months == 12) {
            return total * 0.85; // 15% discount
        }
        return total;
    }

    // Basic members receive a 5% renewal loyalty discount
    @Override
    public double getRenewalDiscountPercentage() {
        return 5.0;
    }
}