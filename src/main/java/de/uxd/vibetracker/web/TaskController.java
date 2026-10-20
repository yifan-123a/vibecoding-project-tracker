package de.uxd.vibetracker.web;

import de.uxd.vibetracker.task.Task;
import de.uxd.vibetracker.task.TaskForm;
import de.uxd.vibetracker.task.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Create / update / delete (M5). Plain form POSTs and redirects — the modal is
 * opened by a GET with a query parameter, so there is no client-side state and
 * a validation error simply re-renders the same page with the modal open.
 */
@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final BoardPage boardPage;

    public TaskController(TaskService taskService, BoardPage boardPage) {
        this.taskService = taskService;
        this.boardPage = boardPage;
    }

    /** Render the board with the modal open and the error messages attached. */
    private String reopenModal(Model model, String mode, Long taskId) {
        boardPage.populate(model);
        model.addAttribute("modal", mode);
        if (taskId != null) {
            model.addAttribute("taskId", taskId);
            taskService.find(taskId).ifPresent(task ->
                    model.addAttribute("contextUpdatedLabel", TimeAgo.format(task.getContextUpdatedAt())));
        }
        return "index";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TaskForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return reopenModal(model, "create", null);
        }
        Task task = taskService.create(form);
        redirect.addFlashAttribute("toast", "Added “" + task.getTitle() + "”.");
        return "redirect:/";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") TaskForm form,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return reopenModal(model, "edit", id);
        }
        Optional<Task> updated = taskService.update(id, form);
        if (updated.isEmpty()) {
            redirect.addFlashAttribute("toast", "That task no longer exists.");
            return "redirect:/";
        }
        redirect.addFlashAttribute("toast", "Saved “" + updated.get().getTitle() + "”.");
        return "redirect:/";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        boolean removed = taskService.delete(id);
        redirect.addFlashAttribute("toast", removed ? "Task deleted." : "That task no longer exists.");
        return "redirect:/";
    }

    /** "Hand off to…" on the card — assignment without opening the modal (M7). */
    @PostMapping("/{id}/assignee")
    public String reassign(@PathVariable Long id, @RequestParam String assignee, RedirectAttributes redirect) {
        if (!BoardPage.TEAM.contains(assignee)) {
            redirect.addFlashAttribute("toast", "Unknown assignee.");
            return "redirect:/";
        }
        Optional<Task> updated = taskService.assign(id, assignee);
        redirect.addFlashAttribute("toast", updated
                .map(task -> "Handed off to " + task.getAssignee() + ".")
                .orElse("That task no longer exists."));
        return "redirect:/";
    }
}
