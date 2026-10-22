package model;

// Concessional membership plan for verified students
public class StudentMembership extends Membership {

    // Constant per-day rate for occasional members
    public static final double DAILY_RATE = 30.0;

    public StudentMembership(String duration) {
        super("Student", duration, 800.0);
    }

    // Student fee calculation: Occasional: 0.0, Monthly: 800, Quarterly: 10% off, Yearly: 25% off
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
            return total * 0.75; // 25% discount
        }
        return total;
    }

    // Polymorphic per-day rate for occasional attendance fee calculation
    @Override
    public double getDailyRate() {
        return DAILY_RATE;
    }

    // Students receive a 15% renewal discount
    @Override
    public double getRenewalDiscountPercentage() {
        return 15.0;
    }
}
