package restaurantrush.entity;

import java.util.List;

/**
 * Role interface for staff the manager directs each turn (Waiter, Chef).
 * ISP: only the methods the controller needs to direct a staff member.
 * OCP: the controller loops over a List of Assignable, so a new directed role
 * is added by implementing this interface, not by editing the controller.
 */
public interface Assignable {

    String getName();

    /** The tasks that are legal right now, for display. */
    List<Task> getAvailableTasks(Restaurant r);

    /**
     * Carries out a task.
     *
     * @return a message describing what happened
     * @throws InvalidActionException if the task breaks a rule; nothing changes
     *         and the staff member may still act this turn
     */
    String perform(Task t, Restaurant r, int turn) throws InvalidActionException;
}
