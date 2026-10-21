package exceptions;

// Thrown when a requested member record cannot be found
public class MemberNotFoundException extends GymException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}