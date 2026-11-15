package restaurantrush.entity;

/** Life cycle of an order, from being taken to being paid or cancelled. */
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    SERVED,
    PAID,
    CANCELLED
}
