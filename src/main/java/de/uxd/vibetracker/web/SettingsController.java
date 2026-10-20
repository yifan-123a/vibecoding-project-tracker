package de.uxd.vibetracker.web;

import de.uxd.vibetracker.settings.SettingsForm;
import de.uxd.vibetracker.settings.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostMapping
    public String save(@ModelAttribute SettingsForm form, RedirectAttributes redirect) {
        settingsService.save(form);
        redirect.addFlashAttribute("toast", "Colours saved.");
        return "redirect:/";
    }

    @PostMapping("/reset")
    public String reset(RedirectAttributes redirect) {
        settingsService.reset();
        redirect.addFlashAttribute("toast", "Colours reset to defaults.");
        return "redirect:/";
    }
}
