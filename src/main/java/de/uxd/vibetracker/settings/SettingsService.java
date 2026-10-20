package de.uxd.vibetracker.settings;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Owns the six user-editable colours (PRD §5 Settings, DESIGN.md §2).
 *
 * The rules, in order of importance:
 *   1. A malformed hex never reaches the database — the previous value stays.
 *   2. A low-contrast choice is allowed but reported, never blocked.
 *   3. Structural colours (surfaces, ink, brand) are not editable here.
 */
@Service
public class SettingsService {

    private static final String SURFACE_WHITE = "#FFFFFF";

    private final SettingsRepository repository;

    public SettingsService(SettingsRepository repository) {
        this.repository = repository;
    }

    /** The singleton row, created on first read. */
    public Settings current() {
        return repository.findById(1L).orElseGet(() -> repository.save(Settings.defaults()));
    }

    public Settings save(SettingsForm form) {
        Settings settings = current();
        settings.setTypeFeature(keep(form.getTypeFeature(), settings.getTypeFeature()));
        settings.setTypeBug(keep(form.getTypeBug(), settings.getTypeBug()));
        settings.setDueSafe(keep(form.getDueSafe(), settings.getDueSafe()));
        settings.setDueWarning(keep(form.getDueWarning(), settings.getDueWarning()));
        settings.setDueOverdue(keep(form.getDueOverdue(), settings.getDueOverdue()));
        settings.setDueNeutral(keep(form.getDueNeutral(), settings.getDueNeutral()));
        return repository.save(settings);
    }

    public Settings reset() {
        Settings settings = current();
        settings.resetToDefaults();
        return repository.save(settings);
    }

    /** Inline custom properties, injected before the stylesheet paints. */
    public String themeCss(Settings settings) {
        return ":root{"
                + "--c-type-feature:" + settings.getTypeFeature() + ";"
                + "--c-type-bug:" + settings.getTypeBug() + ";"
                + "--c-due-safe:" + settings.getDueSafe() + ";"
                + "--c-due-warning:" + settings.getDueWarning() + ";"
                + "--c-due-overdue:" + settings.getDueOverdue() + ";"
                + "--c-due-neutral:" + settings.getDueNeutral() + ";"
                + "}";
    }

    public List<ColorRow> rows(Settings settings) {
        return List.of(
                row("typeFeature", "--c-type-feature", "Feature tasks",
                        "Stripe and chip on feature cards", settings.getTypeFeature()),
                row("typeBug", "--c-type-bug", "Bug tasks",
                        "Stripe and chip on bug cards", settings.getTypeBug()),
                row("dueSafe", "--c-due-safe", "Due in 2+ days",
                        "Calm — nothing to do about it today", settings.getDueSafe()),
                row("dueWarning", "--c-due-warning", "Due today",
                        "Inside the next 24 hours", settings.getDueWarning()),
                row("dueOverdue", "--c-due-overdue", "Past due",
                        "The date has passed", settings.getDueOverdue()),
                row("dueNeutral", "--c-due-neutral", "Done / no date",
                        "Always neutral, overrides the date", settings.getDueNeutral()));
    }

    private static ColorRow row(String field, String cssVar, String label, String hint, String hex) {
        // These colours are used as a 3px stripe, a tinted chip and small due
        // text — never as body copy. So the bar is "can you see it", not WCAG AA
        // for paragraphs; saying otherwise would flag our own defaults as errors.
        double ratio = contrast(hex, SURFACE_WHITE);
        String level = "";
        String note = "";
        if (ratio < 2.0) {
            level = "warn";
            note = String.format(Locale.ROOT,
                    "Very low contrast (%.1f:1) — reads as a block, not as a label.", ratio);
        } else if (ratio < 3.0) {
            level = "warn";
            note = String.format(Locale.ROOT,
                    "Low contrast (%.1f:1) — fine as a stripe or fill, weak as text.", ratio);
        }
        return new ColorRow(field, cssVar, label, hint, hex, level, note);
    }

    private static String keep(String candidate, String fallback) {
        String normalized = normalize(candidate);
        return normalized != null ? normalized : fallback;
    }

    /** '#abc' | 'abc' | '#AABBCC' -> '#AABBCC'; anything else -> null. */
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.length() == 3) {
            StringBuilder expanded = new StringBuilder(6);
            for (char c : value.toCharArray()) {
                expanded.append(c).append(c);
            }
            value = expanded.toString();
        }
        if (!value.matches("[0-9a-fA-F]{6}")) {
            return null;
        }
        return "#" + value.toUpperCase(Locale.ROOT);
    }

    /** WCAG relative luminance, 0 (black) .. 1 (white). */
    static double luminance(String hex) {
        String value = normalize(hex);
        if (value == null) {
            return 0;
        }
        double[] channels = new double[3];
        for (int i = 0; i < 3; i++) {
            int channel = Integer.parseInt(value.substring(1 + i * 2, 3 + i * 2), 16);
            double c = channel / 255.0;
            channels[i] = c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * channels[0] + 0.7152 * channels[1] + 0.0722 * channels[2];
    }

    static double contrast(String a, String b) {
        double la = luminance(a);
        double lb = luminance(b);
        double lighter = Math.max(la, lb);
        double darker = Math.min(la, lb);
        return (lighter + 0.05) / (darker + 0.05);
    }
}
