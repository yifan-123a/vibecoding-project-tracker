package de.uxd.vibetracker.web;

import de.uxd.vibetracker.settings.Settings;
import de.uxd.vibetracker.settings.SettingsService;
import de.uxd.vibetracker.task.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class BoardController {

    private final TaskService taskService;
    private final SettingsService settingsService;

    public BoardController(TaskService taskService, SettingsService settingsService) {
        this.taskService = taskService;
        this.settingsService = settingsService;
    }

    @GetMapping("/")
    public String board(Model model) {
        LocalDate today = LocalDate.now();
        Settings settings = settingsService.current();

        model.addAttribute("columns", taskService.board(today));
        model.addAttribute("settings", settings);
        model.addAttribute("colorRows", settingsService.rows(settings));
        model.addAttribute("themeCss", settingsService.themeCss(settings));
        model.addAttribute("today", today);
        return "index";
    }
}
