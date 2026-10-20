package de.uxd.vibetracker.settings;

/**
 * One row in the settings drawer: what to call the colour, what to bind it to,
 * and whether it is readable.
 *
 * @param level "" | "warn" | "error" — drives the note's styling, never blocks.
 */
public record ColorRow(
        String field,
        String cssVar,
        String label,
        String hint,
        String hex,
        String level,
        String note) {
}
