package de.uxd.vibetracker.task;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {

    /** The only legal values for {@code Task.assignee} (PRD §8). */
    public static final List<String> ASSIGNEES = List.of("Yifan", "AI");

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

    public Optional<Task> find(Long id) {
        return repository.findById(id);
    }

    /** Pre-fill the modal from the entity (M5, edit mode). */
    public TaskForm formFor(Task task) {
        TaskForm form = new TaskForm();
        form.setTitle(task.getTitle());
        form.setDescription(task.getDescription());
        form.setType(task.getType());
        form.setStatus(task.getStatus());
        form.setAssignee(task.getAssignee());
        form.setDueDate(task.getDueDate());
        return form;
    }

    @Transactional
    public Task create(TaskForm form) {
        Task task = new Task(
                form.getTitle().trim(),
                form.getType(),
                form.getStatus(),
                form.getAssignee().trim(),
                form.getDueDate());
        task.setDescription(trimToNull(form.getDescription()));
        return repository.save(task);
    }

    @Transactional
    public Optional<Task> update(Long id, TaskForm form) {
        return repository.findById(id).map(task -> {
            task.setTitle(form.getTitle().trim());
            task.setDescription(trimToNull(form.getDescription()));
            task.setType(form.getType());
            task.setStatus(form.getStatus());
            task.setAssignee(form.getAssignee().trim());
            task.setDueDate(form.getDueDate());
            return repository.save(task);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    /** Hand a task to someone else straight from the card, no modal (M7). */
    @Transactional
    public Optional<Task> assign(Long id, String assignee) {
        if (!ASSIGNEES.contains(assignee)) {
            return Optional.empty();
        }
        return repository.findById(id).map(task -> {
            task.setAssignee(assignee);
            return repository.save(task);
        });
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

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
