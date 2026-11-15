package restaurantrush.entity;

import java.util.Comparator;

/**
 * A food critic: impatient (loses 12 satisfaction per waiting turn) and pays
 * full price. Distinct order preference (documented): the Critic ignores the
 * default rule and always orders the most expensive dish on the menu. In the
 * base setting that is Pasta, the slowest dish, so the manager has to give the
 * Critic priority at the table and in the kitchen.
 */
public class CriticCustomer extends Customer {

    private static final int SATISFACTION_LOSS = 12;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        return menu.getItems().stream()
                .max(Comparator.comparingInt(MenuItem::getPriceCents))
                .orElseThrow();
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }
}
