package de.uxd.vibetracker.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A single vibecoding task — PRD §5. Field names are the contract: they map
 * straight onto the form fields and the copied Markdown block.
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskType type = TaskType.FEATURE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskStatus status = TaskStatus.TODO;

    @Column(nullable = false, length = 60)
    private String assignee = "Yifan";

    private LocalDate dueDate;

    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();

    /** M9: the curated briefing that makes Copy Context worth anything. */
    @Column(length = 4000)
    private String context;

    @Column(length = 40)
    private String contextTool;

    private LocalDateTime contextUpdatedAt;

    protected Task() {
        // for JPA
    }

    public Task(String title, TaskType type, TaskStatus status, String assignee, LocalDate dueDate) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.createdDate = LocalDate.now();
    }

    /** First letters of the assignee, for the avatar badge. */
    public String initials() {
        if (assignee == null || assignee.isBlank()) {
            return "?";
        }
        return assignee.trim().substring(0, Math.min(2, assignee.trim().length())).toUpperCase();
    }

    public Long getId() {
        return id;
    }

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

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContextTool() {
        return contextTool;
    }

    public void setContextTool(String contextTool) {
        this.contextTool = contextTool;
    }

    public LocalDateTime getContextUpdatedAt() {
        return contextUpdatedAt;
    }

    public void setContextUpdatedAt(LocalDateTime contextUpdatedAt) {
        this.contextUpdatedAt = contextUpdatedAt;
    }
}
