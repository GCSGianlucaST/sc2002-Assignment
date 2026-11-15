package restaurantrush.entity;

import java.util.List;

/**
 * Two existing menu items sold together (brief 4.3). Preparation units are
 * the sum of the two; the price is 90% of their combined price, rounded to
 * the nearest cent: Salad + Pasta = ($9 + $18) x 0.9 = $24.30, 3 units.
 *
 * A ComboMeal IS-A MenuItem, so Order, Chef, Waiter and the bill rules handle
 * it unchanged (LSP). It is composed of its two components.
 */
public class ComboMeal extends MenuItem {

    private static final int PRICE_PERCENT = 90;

    private final List<MenuItem> components;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName(),
                Money.percentOf(first.getPriceCents() + second.getPriceCents(), PRICE_PERCENT),
                first.getPreparationUnits() + second.getPreparationUnits());
        this.components = List.of(first, second);
    }

    public boolean contains(MenuItem item) {
        return components.contains(item);
    }

    /** Read-only list of the two components. */
    public List<MenuItem> getComponents() {
        return components;
    }
}
