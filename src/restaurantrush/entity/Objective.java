package restaurantrush.entity;

import java.util.OptionalDouble;

/** The three end-of-game targets. All three must be met for a victory. */
public class Objective {

    private final int minPaidCustomers;
    private final int minRevenueCents;
    private final double minAverageSatisfaction;

    public Objective(int paid, int revenueCents, double avgSat) {
        this.minPaidCustomers = paid;
        this.minRevenueCents = revenueCents;
        this.minAverageSatisfaction = avgSat;
    }

    public GameResult evaluate(Scoreboard s) {
        int paid = s.getPaidCount();
        int revenue = s.getRevenueCents();
        OptionalDouble avg = s.getAverageSatisfaction();
        boolean met = paid >= minPaidCustomers
                && revenue >= minRevenueCents
                && avg.isPresent()
                && avg.getAsDouble() >= minAverageSatisfaction;
        return met ? GameResult.VICTORY : GameResult.DEFEAT;
    }

    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }

    public int getMinRevenueCents() {
        return minRevenueCents;
    }

    public double getMinAverageSatisfaction() {
        return minAverageSatisfaction;
    }
}
