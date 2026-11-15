package restaurantrush.entity;

/**
 * A manager's choice for one staff member, e.g. SEAT customer 3.
 * The UI creates it from text; the staff member decides whether it is legal.
 * Immutable value object.
 */
public class Task {

    private final TaskType type;
    private final int targetId;

    public Task(TaskType type, int targetId) {
        this.type = type;
        this.targetId = targetId;
    }

    /** A ready-made Wait task (no target). */
    public static Task waitTask() {
        return new Task(TaskType.WAIT, 0);
    }

    public TaskType getType() {
        return type;
    }

    public int getTargetId() {
        return targetId;
    }
}
