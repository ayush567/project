package model;

// Concessional membership plan for verified students
public class StudentMembership extends Membership {

    public StudentMembership(String duration) {
        super("Student", duration, 800.0);
    }

    // Student fee calculation: Monthly: 800, Quarterly: 10% off, Yearly: 25% off
    @Override
    public double calculateFee() {
        int months = getDurationMonths();
        double total = getBaseMonthlyRate() * months;
        if (months == 3) {
            return total * 0.90; // 10% discount
        } else if (months == 12) {
            return total * 0.75; // 25% discount
        }
        return total;
    }

    // Students receive a 15% renewal discount
    @Override
    public double getRenewalDiscountPercentage() {
        return 15.0;
    }
}