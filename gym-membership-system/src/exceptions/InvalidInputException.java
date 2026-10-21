package exceptions;

// Thrown when user inputs or parameters fail validation checks
public class InvalidInputException extends GymException {
    public InvalidInputException(String message) {
        super(message);
    }
}