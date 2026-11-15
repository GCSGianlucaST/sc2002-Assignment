package restaurantrush.entity;

/**
 * Life cycle of a customer. Used by the turn rules to decide who loses
 * satisfaction (step 5) and who may pay (step 4).
 */
public enum CustomerStatus {
    WAITING,
    SEATED,
    ORDERED,
    EATING,
    READY_TO_PAY,
    PAID,
    LEFT
}
