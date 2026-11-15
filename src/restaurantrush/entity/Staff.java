package restaurantrush.entity;

/**
 * Common rule for every staff role: one action per turn.
 * Written once here and inherited by Waiter, Chef and Cashier.
 * It does NOT declare perform(): the Cashier acts automatically, so forcing it
 * to accept manager tasks would break LSP/ISP. See {@link Assignable}.
 */
public abstract class Staff {

    private final String name;
    private boolean actedThisTurn;

    protected Staff(String name) {
        this.name = name;
    }

    /** Called at the start of every turn. */
    public void startTurn() {
        actedThisTurn = false;
    }

    public boolean hasActedThisTurn() {
        return actedThisTurn;
    }

    protected void markActed() {
        actedThisTurn = true;
    }

    /** Refuses a second action in the same turn. */
    protected void ensureCanAct() throws InvalidActionException {
        if (actedThisTurn) {
            throw new InvalidActionException(name + " has already acted this turn.");
        }
    }

    public String getName() {
        return name;
    }
}
