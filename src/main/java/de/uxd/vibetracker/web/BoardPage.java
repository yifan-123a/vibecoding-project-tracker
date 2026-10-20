package de.uxd.vibetracker.web;

import de.uxd.vibetracker.settings.Settings;
import de.uxd.vibetracker.settings.SettingsService;
import de.uxd.vibetracker.task.TaskService;
import de.uxd.vibetracker.task.TaskStatus;
import de.uxd.vibetracker.task.TaskType;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.time.LocalDate;
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

    public BoardPage(TaskService taskService, SettingsService settingsService) {
        this.taskService = taskService;
        this.settingsService = settingsService;
    }

    public void populate(Model model) {
        LocalDate today = LocalDate.now();
        Settings settings = settingsService.current();

        model.addAttribute("columns", taskService.board(today));
        model.addAttribute("settings", settings);
        model.addAttribute("colorRows", settingsService.rows(settings));
        model.addAttribute("themeCss", settingsService.themeCss(settings));
        model.addAttribute("today", today);
        model.addAttribute("team", TEAM);
        model.addAttribute("types", TaskType.values());
        model.addAttribute("statuses", TaskStatus.values());
    }
}
