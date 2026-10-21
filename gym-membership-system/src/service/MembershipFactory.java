package service;

import exceptions.InvalidInputException;
import model.BasicMembership;
import model.Membership;
import model.PremiumMembership;
import model.StudentMembership;

// Factory Pattern implementation to instantiate appropriate Membership subclass
public class MembershipFactory {

    public static Membership createMembership(String type, String duration) throws InvalidInputException {
        if (type == null || type.trim().isEmpty()) {
            throw new InvalidInputException("Membership type cannot be empty.");
        }
        if (duration == null || duration.trim().isEmpty()) {
            duration = "Monthly";
        }

        String normalizedType = type.trim();
        if ("Basic".equalsIgnoreCase(normalizedType)) {
            return new BasicMembership(duration);
        } else if ("Premium".equalsIgnoreCase(normalizedType)) {
            return new PremiumMembership(duration);
        } else if ("Student".equalsIgnoreCase(normalizedType)) {
            return new StudentMembership(duration);
        } else {
            throw new InvalidInputException("Unknown membership type: " + type + ". Allowed: Basic, Premium, Student.");
        }
    }
}