package de.uxd.vibetracker.task;

/**
 * Derived per render — never stored. Drives the card tint (M8).
 *
 * The milestone spec defines four buckets and leaves a gap between 24 h and 48 h.
 * Decision (DESIGN.md §2): that window folds into SAFE, so a card only turns
 * amber inside the last day.
 */
public enum DueState {
    OVERDUE("overdue"),
    WARNING("warning"),
    SAFE("safe"),
    NEUTRAL("neutral");

    private final String cssClass;

    DueState(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getCssClass() {
        return cssClass;
    }
}
