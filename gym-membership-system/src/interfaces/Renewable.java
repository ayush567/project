package interfaces;

// Interface defining renewal and fee calculation contract
public interface Renewable {
    double calculateFee();
    double getRenewalDiscountPercentage();
    double calculateRenewalFee();
}