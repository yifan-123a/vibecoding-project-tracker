package de.uxd.vibetracker.settings;

/** Form backing object for POST /settings. Plain setters — Thymeleaf binds to them. */
public class SettingsForm {

    private String typeFeature;
    private String typeBug;
    private String dueSafe;
    private String dueWarning;
    private String dueOverdue;
    private String dueNeutral;

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
