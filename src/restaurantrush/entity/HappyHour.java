package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Happy Hour pricing policy (brief 4.3). The manager may switch it on
 * once per game, at the start of a turn; it lasts that turn and the next.
 * While active, orders taken get 20% off (applied after any customer
 * discount and locked into the order), and customers still waiting for a
 * table or food recover 15 satisfaction at the start of each active turn.
 */
public class HappyHour {

    public static final int DURATION_TURNS = 2;
    public static final int BILL_PERCENT = 80;
    public static final int RECOVERY = 15;

    private boolean used;
    private int turnsRemaining;

    public boolean canActivate() {
        return !used;
    }

    public void activate() {
        if (used) {
            throw new IllegalStateException("Happy Hour can only be activated once per game");
        }
        used = true;
        turnsRemaining = DURATION_TURNS;
    }

    public boolean isActive() {
        return turnsRemaining > 0;
    }

    /** Applied to a bill that already includes the customer's own discount. */
    public int applyDiscount(int billCents) {
        return isActive() ? Money.percentOf(billCents, BILL_PERCENT) : billCents;
    }

    /**
     * Start of an active turn: every customer still awaiting service recovers.
     *
     * @return the customers whose satisfaction actually rose
     */
    public List<Customer> recoverWaiting(List<Customer> customers) {
        List<Customer> recovered = new ArrayList<>();
        if (!isActive()) {
            return recovered;
        }
        for (Customer c : customers) {
            if (c.isAwaitingService()) {
                int before = c.getSatisfaction();
                c.recoverSatisfaction(RECOVERY);
                if (c.getSatisfaction() > before) {
                    recovered.add(c);
                }
            }
        }
        return recovered;
    }

    /** Called once at the end of every turn. */
    public void endTurn() {
        if (turnsRemaining > 0) {
            turnsRemaining--;
        }
    }

    /** Active turns left, counting the current one (2 = this turn and the next). */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public boolean isUsed() {
        return used;
    }
}
