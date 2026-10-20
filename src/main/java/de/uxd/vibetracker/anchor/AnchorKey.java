package de.uxd.vibetracker.anchor;

/**
 * The four final-project deliverables, pinned above the board (M11).
 * Declaration order is render order — do not reshuffle without checking the UI.
 */
public enum AnchorKey {
    PRESENTATION("Presentation", "Final deck"),
    DEMO("Demo", "Live link"),
    REPORT("Report", "Written report"),
    DOCUMENTATION("Documentation", "Docs or repo");

    private final String label;
    private final String hint;

    AnchorKey(String label, String hint) {
        this.label = label;
        this.hint = hint;
    }

    public String getLabel() {
        return label;
    }

    public String getHint() {
        return hint;
    }
}
