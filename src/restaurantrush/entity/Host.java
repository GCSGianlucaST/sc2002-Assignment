package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Seats at most one queued customer per turn, in addition to the Waiter's
 * task (brief 4.3). It uses the same seating rule as the Waiter, kept in
 * {@link Restaurant#seatFromQueue}. Being Assignable, it is directed by the
 * controller's existing staff loop with no controller change.
 */
public class Host extends Staff implements Assignable {

    public Host() {
        super("Host");
    }

    @Override
    public List<Task> getAvailableTasks(Restaurant r) {
        List<Task> tasks = new ArrayList<>();
        if (r.findFreeTable().isPresent()) {
            for (Customer c : r.getWaitingQueue().getCustomers()) {
                tasks.add(new Task(TaskType.SEAT, c.getId()));
            }
        }
        tasks.add(Task.waitTask());
        return tasks;
    }

    @Override
    public String perform(Task t, Restaurant r, int turn) throws InvalidActionException {
        ensureCanAct();
        String msg = switch (t.getType()) {
            case SEAT -> {
                Table table = r.seatFromQueue(t.getTargetId());
                yield "Host seats Customer #" + t.getTargetId() + " at table " + table.getId() + ".";
            }
            case WAIT -> "Host waits this turn.";
            default -> throw new InvalidActionException("The Host can only seat customers or wait.");
        };
        markActed();
        return msg;
    }
}
