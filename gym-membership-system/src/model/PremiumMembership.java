package model;

// Premium membership including personal trainer and special amenities
public class PremiumMembership extends Membership {

    public PremiumMembership(String duration) {
        super("Premium", duration, 2000.0);
    }

    // Premium fee calculation: Monthly: 2000, Quarterly: 10% off, Yearly: 20% off
    @Override
    public double calculateFee() {
        int months = getDurationMonths();
        double total = getBaseMonthlyRate() * months;
        if (months == 3) {
            return total * 0.90; // 10% discount
        } else if (months == 12) {
            return total * 0.80; // 20% discount
        }
        return total;
    }

    // Premium members receive a 10% renewal loyalty discount
    @Override
    public double getRenewalDiscountPercentage() {
        return 10.0;
    }
}