package de.uxd.vibetracker.task;

/** Feature vs. bug — the only dimension with a colour of its own (M6). */
public enum TaskType {
    FEATURE("Feature"),
    BUG("Bug");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
