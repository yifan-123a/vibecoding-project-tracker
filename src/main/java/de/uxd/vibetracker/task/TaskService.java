package de.uxd.vibetracker.task;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    private static final Comparator<TaskView> CARD_ORDER =
            Comparator.comparingInt((TaskView view) -> view.dueState().ordinal());

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    /** Four columns in the canonical order, empty ones included. */
    public List<ColumnView> board(LocalDate today) {
        Map<TaskStatus, List<TaskView>> grouped = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            grouped.put(status, new ArrayList<>());
        }
        for (Task task : repository.findAll()) {
            grouped.get(task.getStatus()).add(toView(task, today));
        }

        List<ColumnView> columns = new ArrayList<>(TaskStatus.values().length);
        for (TaskStatus status : TaskStatus.values()) {
            List<TaskView> tasks = grouped.get(status);
            tasks.sort(CARD_ORDER);
            columns.add(new ColumnView(status.getId(), status.getLabel(), tasks));
        }
        return columns;
    }

    TaskView toView(Task task, LocalDate today) {
        return new TaskView(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getType(),
                task.getStatus(),
                task.getAssignee(),
                task.initials(),
                dueState(task, today),
                dueLabel(task, today));
    }

    static DueState dueState(Task task, LocalDate today) {
        if (task.getStatus() == TaskStatus.DONE || task.getDueDate() == null) {
            return DueState.NEUTRAL;
        }
        long days = ChronoUnit.DAYS.between(today, task.getDueDate());
        if (days < 0) {
            return DueState.OVERDUE;
        }
        if (days == 0) {
            return DueState.WARNING;
        }
        return DueState.SAFE;
    }

    static String dueLabel(Task task, LocalDate today) {
        if (task.getStatus() == TaskStatus.DONE) {
            return "Done";
        }
        if (task.getDueDate() == null) {
            return "No due date";
        }
        long days = ChronoUnit.DAYS.between(today, task.getDueDate());
        if (days < 0) {
            long late = -days;
            return "Overdue by " + late + (late == 1 ? " day" : " days");
        }
        if (days == 0) {
            return "Due today";
        }
        if (days == 1) {
            return "Due tomorrow";
        }
        return "Due in " + days + " days";
    }
}
