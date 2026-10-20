package de.uxd.vibetracker.web;

import de.uxd.vibetracker.anchor.AnchorKey;
import de.uxd.vibetracker.anchor.AnchorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/anchors")
public class AnchorController {

    private final AnchorService anchorService;

    public AnchorController(AnchorService anchorService) {
        this.anchorService = anchorService;
    }

    @PostMapping("/{key}")
    public String save(@PathVariable AnchorKey key,
                       @RequestParam(required = false) String url,
                       RedirectAttributes redirect) {
        if (!anchorService.save(key, url)) {
            redirect.addFlashAttribute("toast",
                    "That does not look like a link — use http:// or https://.");
            return "redirect:/";
        }
        boolean cleared = url == null || url.isBlank();
        redirect.addFlashAttribute("toast", cleared
                ? key.getLabel() + " link cleared."
                : key.getLabel() + " link saved.");
        return "redirect:/";
    }
}
