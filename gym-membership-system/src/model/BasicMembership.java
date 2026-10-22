package model;

// Basic membership offering standard gym access
public class BasicMembership extends Membership {

    // Constant per-day rate for occasional members
    public static final double DAILY_RATE = 50.0;

    public BasicMembership(String duration) {
        super("Basic", duration, 1000.0);
    }

    // Standard fee calculation: Occasional: 0.0, Monthly: 1000, Quarterly: 5% off, Yearly: 15% off
    @Override
    public double calculateFee() {
        if ("Occasional".equalsIgnoreCase(getDuration())) {
            return 0.0;
        }
        int months = getDurationMonths();
        double total = getBaseMonthlyRate() * months;
        if (months == 3) {
            return total * 0.95; // 5% discount
        } else if (months == 12) {
            return total * 0.85; // 15% discount
        }
        return total;
    }

    // Polymorphic per-day rate for occasional attendance fee calculation
    @Override
    public double getDailyRate() {
        return DAILY_RATE;
    }

    // Basic members receive a 5% renewal loyalty discount
    @Override
    public double getRenewalDiscountPercentage() {
        return 5.0;
    }
}
