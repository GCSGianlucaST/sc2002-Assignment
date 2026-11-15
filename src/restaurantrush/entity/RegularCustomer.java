package restaurantrush.entity;

/**
 * The standard customer: loses 8 satisfaction per waiting turn, pays full
 * price and uses the default food rule in {@link Customer#chooseMenuItem}.
 */
public class RegularCustomer extends Customer {

    private static final int SATISFACTION_LOSS = 8;

    public RegularCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
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
