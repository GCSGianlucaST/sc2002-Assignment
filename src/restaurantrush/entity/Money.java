package restaurantrush.entity;

/**
 * Money is stored as integer cents everywhere (brief section 2.2).
 * This is the single place that turns cents into a dollar string.
 */
public final class Money {

    private Money() {
        // utility class: no instances
    }

    /** Formats integer cents as dollars with two decimals, e.g. 1800 becomes "$18.00". */
    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    /** {@code percent}% of an amount, rounded to the nearest cent, e.g. percentOf(1500, 90) is 1350. */
    public static int percentOf(int cents, int percent) {
        return (int) Math.round(cents * percent / 100.0);
    }
}
