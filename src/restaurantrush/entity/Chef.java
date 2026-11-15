package restaurantrush.entity;

import java.util.ArrayList;
import java.util.List;

/** Advances one order by one preparation unit per turn. */
public class Chef extends Staff implements Assignable {

    public Chef() {
        super("Chef");
    }

    @Override
    public List<Task> getAvailableTasks(Restaurant r) {
        List<Task> tasks = new ArrayList<>();
        for (Order o : r.getActiveOrders()) {
            if (o.isCookable()) {
                tasks.add(new Task(TaskType.PREPARE, o.getId()));
            }
        }
        tasks.add(Task.waitTask());
        return tasks;
    }

    @Override
    public String perform(Task t, Restaurant r, int turn) throws InvalidActionException {
        ensureCanAct();
        String msg = switch (t.getType()) {
            case PREPARE -> prepare(t.getTargetId(), r);
            case WAIT -> "Chef waits this turn.";
            default -> throw new InvalidActionException("The Chef can only cook or wait.");
        };
        markActed();
        return msg;
    }

    private String prepare(int orderId, Restaurant r) throws InvalidActionException {
        Order o = r.findOrder(orderId).orElseThrow(() -> new InvalidActionException(
                "There is no active order #" + orderId + "."));
        boolean cookable = o.isCookable();
        if (!cookable) {
            throw new InvalidActionException("Order #" + orderId + " is " + o.getStatus()
                    + " and needs no more cooking.");
        }
        o.advancePreparation();
        int units = o.getItem().getPreparationUnits();
        int done = units - o.getRemainingUnits();
        String progress = o.isReady() ? "READY" : "PREPARING";
        return "Chef works on order #" + orderId + " (" + o.getItem().getName() + "): "
                + done + "/" + units + " units, " + progress + ".";
    }
}
