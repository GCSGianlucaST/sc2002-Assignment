package restaurantrush.entity;

/**
 * Thrown when the manager gives a staff member a task that breaks the rules.
 * It is a checked exception, so the compiler forces the caller to handle it:
 * the controller shows the reason and asks again, and the staff member's task
 * for the turn is not used up (brief section 3.4).
 */
public class InvalidActionException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidActionException(String message) {
        super(message);
    }
}
