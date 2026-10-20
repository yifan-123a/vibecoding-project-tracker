package de.uxd.vibetracker.web;

import de.uxd.vibetracker.task.Task;
import de.uxd.vibetracker.task.TaskForm;
import de.uxd.vibetracker.task.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
public class BoardController {

    private final BoardPage boardPage;
    private final TaskService taskService;

    public BoardController(BoardPage boardPage, TaskService taskService) {
        this.boardPage = boardPage;
        this.taskService = taskService;
    }

    /**
     * The board, optionally with the task modal open:
     *   /             → plain board
     *   /?q=context   → board filtered by a free-text query (extra feature)
     *   /?create      → empty modal
     *   /?edit={id}   → modal pre-filled from the database
     */
    @GetMapping("/")
    public String board(@RequestParam(required = false) Long edit,
                        @RequestParam(required = false) Boolean create,
                        @RequestParam(required = false) String q,
                        Model model,
                        RedirectAttributes redirect) {
        boardPage.populate(model, q);

        if (edit != null) {
            Optional<Task> task = taskService.find(edit);
            if (task.isEmpty()) {
                redirect.addFlashAttribute("toast", "That task no longer exists.");
                return "redirect:/";
            }
            model.addAttribute("form", taskService.formFor(task.get()));
            model.addAttribute("modal", "edit");
            model.addAttribute("taskId", task.get().getId());
            model.addAttribute("contextUpdatedLabel", TimeAgo.format(task.get().getContextUpdatedAt()));
        } else if (Boolean.TRUE.equals(create)) {
            model.addAttribute("form", new TaskForm());
            model.addAttribute("modal", "create");
        }

        return "index";
    }

    /**
     * The whole board as Markdown (extra feature): a status update you can paste
     * into a chat or a commit message. Same columns, same order, no filtering.
     */
    @GetMapping(value = "/board.md", produces = "text/markdown;charset=UTF-8")
    @ResponseBody
    public String markdown() {
        return BoardMarkdown.render(taskService.board(java.time.LocalDateTime.now()), LocalDate.now());
    }
}
