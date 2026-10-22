package model;

// Premium membership including personal trainer and special amenities
public class PremiumMembership extends Membership {

    // Constant per-day rate for occasional members
    public static final double DAILY_RATE = 100.0;

    public PremiumMembership(String duration) {
        super("Premium", duration, 2000.0);
    }

    // Premium fee calculation: Occasional: 0.0, Monthly: 2000, Quarterly: 10% off, Yearly: 20% off
    @Override
    public double calculateFee() {
        if ("Occasional".equalsIgnoreCase(getDuration())) {
            return 0.0;
        }
        int months = getDurationMonths();
        double total = getBaseMonthlyRate() * months;
        if (months == 3) {
            return total * 0.90; // 10% discount
        } else if (months == 12) {
            return total * 0.80; // 20% discount
        }
        return total;
    }

    // Polymorphic per-day rate for occasional attendance fee calculation
    @Override
    public double getDailyRate() {
        return DAILY_RATE;
    }

    // Premium members receive a 10% renewal loyalty discount
    @Override
    public double getRenewalDiscountPercentage() {
        return 10.0;
    }
}
