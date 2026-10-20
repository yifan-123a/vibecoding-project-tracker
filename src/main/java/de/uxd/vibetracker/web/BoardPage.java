package de.uxd.vibetracker.web;

import de.uxd.vibetracker.anchor.AnchorService;
import de.uxd.vibetracker.settings.Settings;
import de.uxd.vibetracker.settings.SettingsService;
import de.uxd.vibetracker.task.ColumnView;
import de.uxd.vibetracker.task.TaskService;
import de.uxd.vibetracker.task.TaskStatus;
import de.uxd.vibetracker.task.TaskType;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Everything the board template needs, in one place — so the GET handler and the
 * POST handlers that re-render on a validation error cannot drift apart.
 */
@Component
public class BoardPage {

    /** Sole assignment target plus the AI pseudo-assignee (PRD §8). */
    public static final List<String> TEAM = TaskService.ASSIGNEES;

    private final TaskService taskService;
    private final SettingsService settingsService;
    private final AnchorService anchorService;

    public BoardPage(TaskService taskService, SettingsService settingsService, AnchorService anchorService) {
        this.taskService = taskService;
        this.settingsService = settingsService;
        this.anchorService = anchorService;
    }

    public void populate(Model model) {
        populate(model, null);
    }

    public void populate(Model model, String query) {
        LocalDateTime now = LocalDateTime.now();
        Settings settings = settingsService.current();

        List<ColumnView> columns = taskService.board(now, query);
        int visible = columns.stream().mapToInt(ColumnView::getCount).sum();

        model.addAttribute("columns", columns);
        model.addAttribute("settings", settings);
        model.addAttribute("colorRows", settingsService.rows(settings));
        model.addAttribute("themeCss", settingsService.themeCss(settings));
        model.addAttribute("today", now.toLocalDate());
        model.addAttribute("team", TEAM);
        model.addAttribute("aiTools", TaskService.AI_TOOLS);
        model.addAttribute("types", TaskType.values());
        model.addAttribute("statuses", TaskStatus.values());

        // M11: the four pinned deliverables, always present.
        model.addAttribute("anchors", anchorService.views());

        // Extra: server-side filter, expressed in the URL.
        model.addAttribute("q", query);
        model.addAttribute("filtering", query != null && !query.isBlank());
        model.addAttribute("visibleTasks", visible);
        model.addAttribute("totalTasks", taskService.countAll());
    }
}
