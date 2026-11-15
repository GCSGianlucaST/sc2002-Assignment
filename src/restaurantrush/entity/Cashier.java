package restaurantrush.entity;

import java.util.Optional;

/**
 * Collects at most one payment per turn, automatically, oldest READY_TO_PAY
 * customer first. Not Assignable: the manager does not direct the Cashier.
 */
public class Cashier extends Staff {

    public Cashier() {
        super("Cashier");
    }

    /**
     * Charges the oldest customer who became READY_TO_PAY on an earlier turn.
     *
     * @return the customer who paid, or empty if nobody could pay
     */
    public Optional<Customer> processPayment(Restaurant r, int turn) {
        if (hasActedThisTurn()) {
            return Optional.empty();
        }
        Customer oldest = null;
        for (Customer c : r.getSeatedCustomers()) {
            boolean ok = c.canPay(turn);
            if (ok && (oldest == null || isOlder(c, oldest))) {
                oldest = c;
            }
        }
        if (oldest == null) {
            return Optional.empty();
        }
        Order o = oldest.getOrder().orElseThrow();
        int bill = o.getBillCents();
        o.markPaid();
        oldest.markPaid();
        Scoreboard sb = r.getScoreboard();
        sb.recordPayment(bill, oldest.getSatisfaction());
        r.removeCustomer(oldest);
        markActed();
        return Optional.of(oldest);
    }

    /** Older = became READY_TO_PAY earlier; ties go to the lower customer number. */
    private boolean isOlder(Customer a, Customer b) {
        if (a.getReadyToPayTurn() != b.getReadyToPayTurn()) {
            return a.getReadyToPayTurn() < b.getReadyToPayTurn();
        }
        return a.getId() < b.getId();
    }
}
