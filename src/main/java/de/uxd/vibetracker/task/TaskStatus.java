package de.uxd.vibetracker.task;

/**
 * The four columns, in render order. The ids match PRD §7 and the `status`
 * field on every task; do not rename without a migration.
 */
public enum TaskStatus {
    TODO("todo", "To Do"),
    IN_PROGRESS("in-progress", "In Progress"),
    REVIEW("review", "Review"),
    DONE("done", "Done");

    private final String id;
    private final String label;

    TaskStatus(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
