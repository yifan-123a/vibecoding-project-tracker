package de.uxd.vibetracker.anchor;

/**
 * What the banner renders: the four keys are always present, filled or not.
 */
public record AnchorView(String key, String label, String hint, String url, boolean filled, String updatedLabel) {
}
