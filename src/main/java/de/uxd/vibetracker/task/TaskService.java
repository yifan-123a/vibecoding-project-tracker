package de.uxd.vibetracker.task;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {

    /** The only legal values for {@code Task.assignee} (PRD §8). */
    public static final List<String> ASSIGNEES = List.of("Yifan", "AI");

    /** M9: the tools a context can be written for. Empty means "not set". */
    public static final List<String> AI_TOOLS =
            List.of("Claude", "ChatGPT", "Cursor", "Lovable", "Replit", "Other");

    private static final Comparator<TaskView> CARD_ORDER =
            Comparator.comparingInt((TaskView view) -> view.dueState().ordinal());

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    /** Four columns in the canonical order, empty ones included. */
    public List<ColumnView> board(LocalDateTime now) {
        return board(now, null);
    }

    /**
     * Same board, optionally filtered by a free-text query (extra feature).
     * Matching happens server-side so the filter is a URL, not client state:
     * {@code /?q=context} is shareable and survives a refresh.
     */
    public List<ColumnView> board(LocalDateTime now, String query) {
        String needle = query == null || query.isBlank() ? null : query.trim().toLowerCase(Locale.ROOT);

        Map<TaskStatus, List<TaskView>> grouped = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            grouped.put(status, new ArrayList<>());
        }
        for (Task task : repository.findAll()) {
            if (needle != null && !matches(task, needle)) {
                continue;
            }
            grouped.get(task.getStatus()).add(toView(task, now));
        }

        List<ColumnView> columns = new ArrayList<>(TaskStatus.values().length);
        for (TaskStatus status : TaskStatus.values()) {
            List<TaskView> tasks = grouped.get(status);
            tasks.sort(CARD_ORDER);
            columns.add(new ColumnView(status.getId(), status.getLabel(), tasks));
        }
        return columns;
    }

    public int countAll() {
        return (int) repository.count();
    }

    private static boolean matches(Task task, String needle) {
        return contains(task.getTitle(), needle)
                || contains(task.getDescription(), needle)
                || contains(task.getContext(), needle)
                || contains(task.getAssignee(), needle);
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
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
        form.setContext(task.getContext());
        form.setContextTool(task.getContextTool());
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
        String context = trimToNull(form.getContext());
        task.setContext(context);
        task.setContextTool(contextTool(form.getContextTool()));
        if (context != null) {
            // M9: the timestamp is set by the service, never typed by the user.
            task.setContextUpdatedAt(LocalDateTime.now());
        }
        return repository.save(task);
    }

    @Transactional
    public Optional<Task> update(Long id, TaskForm form) {
        return repository.findById(id).map(task -> {
            String context = trimToNull(form.getContext());
            // Only a real edit to the text bumps the "updated N ago" stamp;
            // saving the form for another field must not make it lie.
            if (!java.util.Objects.equals(context, task.getContext())) {
                task.setContextUpdatedAt(context == null ? null : LocalDateTime.now());
            }
            task.setContext(context);
            task.setContextTool(contextTool(form.getContextTool()));
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

    TaskView toView(Task task, LocalDateTime now) {
        return new TaskView(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getType(),
                task.getStatus(),
                task.getAssignee(),
                task.initials(),
                dueState(task, now),
                dueLabel(task, now.toLocalDate()));
    }

    /**
     * The milestone says "less than 24 hours", so the window is measured from
     * the end of the due day, not from midnight. A task due today turns amber
     * as soon as it is inside that window; everything else stays calm.
     */
    static DueState dueState(Task task, LocalDateTime now) {
        if (task.getStatus() == TaskStatus.DONE || task.getDueDate() == null) {
            return DueState.NEUTRAL;
        }
        LocalDateTime endOfDueDay = task.getDueDate().plusDays(1).atStartOfDay();
        long hours = ChronoUnit.HOURS.between(now, endOfDueDay);
        if (hours < 0) {
            return DueState.OVERDUE;
        }
        if (hours <= 24) {
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

    /** Only the six known tools are stored; anything else becomes "not set". */
    private static String contextTool(String candidate) {
        return AI_TOOLS.contains(candidate) ? candidate : null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
