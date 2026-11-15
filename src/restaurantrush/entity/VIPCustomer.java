package restaurantrush.entity;

/**
 * A VIP: more patient (loses 5 satisfaction per waiting turn) and gets 10%
 * off the bill. Orders with the default food rule.
 */
public class VIPCustomer extends Customer {

    private static final int SATISFACTION_LOSS = 5;
    private static final int BILL_PERCENT = 90;

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    /** 10% off the menu price, rounded to the nearest cent. */
    @Override
    public int calculateBillCents(MenuItem item) {
        return Money.percentOf(item.getPriceCents(), BILL_PERCENT);
    }
}
