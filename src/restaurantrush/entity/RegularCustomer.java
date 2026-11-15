package restaurantrush.entity;

/**
 * The Stage 1 customer.
 * Food rule (documented): customer #n orders menu item (n - 1) mod menu size,
 * so with [Salad, Burger, Pasta] the six arrivals order
 * Salad, Burger, Pasta, Salad, Burger, Pasta.
 */
public class RegularCustomer extends Customer {

    private static final int SATISFACTION_LOSS = 8;

    public RegularCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        int n = menu.size();
        return menu.getItem((getId() - 1) % n);
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "Regular";
    }
}
