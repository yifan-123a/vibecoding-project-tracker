package de.uxd.vibetracker.settings;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The user's colour choices — one row, id = 1, never deleted (PRD §5 Settings).
 * Values are {@code #RRGGBB}; anything malformed is rejected by the service and
 * the default stays.
 */
@Entity
@Table(name = "settings")
public class Settings {

    public static final String DEFAULT_TYPE_FEATURE = "#10B981";
    public static final String DEFAULT_TYPE_BUG = "#DC2626";
    public static final String DEFAULT_DUE_SAFE = "#64748B";
    public static final String DEFAULT_DUE_WARNING = "#F59E0B";
    public static final String DEFAULT_DUE_OVERDUE = "#DC2626";
    public static final String DEFAULT_DUE_NEUTRAL = "#94A3B8";

    @Id
    private Long id = 1L;

    private String typeFeature = DEFAULT_TYPE_FEATURE;
    private String typeBug = DEFAULT_TYPE_BUG;
    private String dueSafe = DEFAULT_DUE_SAFE;
    private String dueWarning = DEFAULT_DUE_WARNING;
    private String dueOverdue = DEFAULT_DUE_OVERDUE;
    private String dueNeutral = DEFAULT_DUE_NEUTRAL;

    protected Settings() {
        // for JPA
    }

    public static Settings defaults() {
        return new Settings();
    }

    public void resetToDefaults() {
        this.typeFeature = DEFAULT_TYPE_FEATURE;
        this.typeBug = DEFAULT_TYPE_BUG;
        this.dueSafe = DEFAULT_DUE_SAFE;
        this.dueWarning = DEFAULT_DUE_WARNING;
        this.dueOverdue = DEFAULT_DUE_OVERDUE;
        this.dueNeutral = DEFAULT_DUE_NEUTRAL;
    }

    public Long getId() {
        return id;
    }

    public String getTypeFeature() {
        return typeFeature;
    }

    public void setTypeFeature(String typeFeature) {
        this.typeFeature = typeFeature;
    }

    public String getTypeBug() {
        return typeBug;
    }

    public void setTypeBug(String typeBug) {
        this.typeBug = typeBug;
    }

    public String getDueSafe() {
        return dueSafe;
    }

    public void setDueSafe(String dueSafe) {
        this.dueSafe = dueSafe;
    }

    public String getDueWarning() {
        return dueWarning;
    }

    public void setDueWarning(String dueWarning) {
        this.dueWarning = dueWarning;
    }

    public String getDueOverdue() {
        return dueOverdue;
    }

    public void setDueOverdue(String dueOverdue) {
        this.dueOverdue = dueOverdue;
    }

    public String getDueNeutral() {
        return dueNeutral;
    }

    public void setDueNeutral(String dueNeutral) {
        this.dueNeutral = dueNeutral;
    }
}
