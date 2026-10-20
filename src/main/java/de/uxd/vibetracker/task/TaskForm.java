package de.uxd.vibetracker.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Backing object for the add / edit modal (M5).
 *
 * Deliberately separate from {@link Task}: the entity owns persistence and
 * {@code createdDate}, the form owns what a human is allowed to type.
 */
public class TaskForm {

    @NotBlank(message = "Give the task a title.")
    @Size(max = 120, message = "Keep the title under 120 characters.")
    private String title;

    @Size(max = 2000, message = "Keep the description under 2000 characters.")
    private String description;

    @NotNull(message = "Pick a type.")
    private TaskType type = TaskType.FEATURE;

    @NotNull(message = "Pick a status.")
    private TaskStatus status = TaskStatus.TODO;

    @NotBlank(message = "Pick an assignee.")
    private String assignee = "Yifan";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
