package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * The restaurant's results. Revenue changes only on payment, so unpaid and
 * abandoned orders never count. Satisfaction is recorded at the moment of
 * payment; with no payments the average is empty (shown as N/A).
 */
public class Scoreboard {

    private int revenueCents;
    private final List<Integer> paidSatisfactions = new ArrayList<>();
    private int servedCount;
    private int unhappyDepartures;

    public void recordServed() {
        servedCount++;
    }

    public void recordPayment(int billCents, int satisfaction) {
        revenueCents += billCents;
        paidSatisfactions.add(satisfaction);
    }

    public void recordDeparture() {
        unhappyDepartures++;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    /** Read-only copy of the satisfaction of each paid customer, in payment order. */
    public List<Integer> getPaidSatisfactions() {
        return List.copyOf(paidSatisfactions);
    }

    public int getPaidCount() {
        return paidSatisfactions.size();
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    /** Average satisfaction of paid customers; empty if nobody has paid. */
    public OptionalDouble getAverageSatisfaction() {
        return paidSatisfactions.stream().mapToInt(Integer::intValue).average();
    }
}
