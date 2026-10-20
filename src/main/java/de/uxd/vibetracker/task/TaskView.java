package de.uxd.vibetracker.task;

/**
 * What a card needs, already resolved: the template does no arithmetic.
 */
public record TaskView(
        Long id,
        String title,
        String description,
        TaskType type,
        TaskStatus status,
        String assignee,
        String initials,
        DueState dueState,
        String dueLabel) {
}
