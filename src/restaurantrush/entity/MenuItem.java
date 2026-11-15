package restaurantrush.entity;

/**
 * A dish on the menu: its name, price in cents and how many preparation
 * units the Chef needs. Salad, Burger and Pasta are instances, not subclasses,
 * because they differ only in data. The class is deliberately not final.
 */
public class MenuItem {

    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String name, int priceCents, int units) {
        if (priceCents < 0 || units < 1) {
            throw new IllegalArgumentException("Price must be >= 0 and units >= 1");
        }
        this.name = name;
        this.priceCents = priceCents;
        this.preparationUnits = units;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }
}
