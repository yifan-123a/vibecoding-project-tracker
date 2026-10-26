package de.uxd.vibetracker.seed;

import de.uxd.vibetracker.anchor.AnchorKey;
import de.uxd.vibetracker.anchor.AnchorRepository;
import de.uxd.vibetracker.anchor.AnchorSlot;
import de.uxd.vibetracker.task.Task;
import de.uxd.vibetracker.task.TaskRepository;
import de.uxd.vibetracker.task.TaskStatus;
import de.uxd.vibetracker.task.TaskType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Demo dataset (M13): eight tasks, two per column, dates relative to today so the
 * board always looks alive. One task carries a full context block — that is the
 * task the pitch copies from — and one carries a short one, so the Copy Context
 * feature can be shown twice without repeating itself.
 */
@Configuration
public class SeedData {

    private static final String DEMO_REPO = "https://github.com/yifan-123a/vibecoding-project-tracker";

    @Bean
    CommandLineRunner seed(TaskRepository tasks, AnchorRepository anchors) {
        return args -> {
            // Settings are created lazily by SettingsService on first use; doing it
            // here as well raced the first request and produced a duplicate key.
            seedAnchor(anchors, AnchorKey.DOCUMENTATION, DEMO_REPO);
            if (tasks.count() > 0) {
                return;
            }

            LocalDate today = LocalDate.now();

            // --- To Do -------------------------------------------------------
            Task readme = new Task("Write the one-page README", TaskType.FEATURE,
                    TaskStatus.TODO, "Yifan", today.plusDays(4));
            readme.setDescription("Four columns, one copy button. Anyone should understand the board in thirty seconds.");

            Task drift = new Task("Column headers drift at 1280 px", TaskType.BUG,
                    TaskStatus.TODO, "AI", today.plusDays(7));
            drift.setDescription("The count pill jumps a pixel when the label wraps, which makes the whole column look crooked.");

            // --- In Progress -------------------------------------------------
            Task contextField = new Task("Ship the Context field", TaskType.FEATURE,
                    TaskStatus.IN_PROGRESS, "Yifan", today);
            contextField.setDescription("The briefing that makes the copy button worth pressing.");
            contextField.setContext("""
                    **Background**
                    The board tracks tasks, but the reason this tool exists is the briefing. Every new chat
                    currently starts by re-explaining the project from scratch, and the model re-derives
                    decisions we already made.

                    **Constraints**
                    Plain Markdown only — no rich text editor, no attachments. Whatever is written here has to
                    survive being pasted into any AI chat, so no HTML and no images. The field must not become
                    a second description: it has to stay small enough to read in one screen.

                    **Tried so far**
                    A single "notes" textarea was the first attempt. It collapsed into a second description
                    because nothing in the interface asked for structure.

                    **Pick up**
                    Keep the four headings as the placeholder, set the updated-at stamp only when the text
                    really changes, then wire Copy Context to the markdown endpoint.
                    """);
            contextField.setContextTool("Claude");
            contextField.setContextUpdatedAt(LocalDateTime.now().minusHours(2));

            Task deploy = new Task("Put the demo online before the pitch", TaskType.FEATURE,
                    TaskStatus.IN_PROGRESS, "AI", today.minusDays(1));
            deploy.setDescription("A localhost link is worthless to an assessor. Free tiers sleep, so warm it up first.");

            // --- Review ------------------------------------------------------
            Task handle = new Task("Drag handle overlaps the due label", TaskType.BUG,
                    TaskStatus.REVIEW, "Yifan", today.plusDays(1));
            handle.setDescription("On narrow cards the handle sits on top of the due label; it should only appear on hover.");

            Task export = new Task("Export the board as Markdown", TaskType.FEATURE,
                    TaskStatus.REVIEW, "AI", today.plusDays(3));
            export.setDescription("A status update that writes itself, without retyping the board.");
            export.setContext("""
                    **Background**
                    The weekly update is retyped by hand even though the board already knows the answer.

                    **Pick up**
                    Generate it from the same serialiser as Copy Context, so there is one markdown dialect.
                    """);
            export.setContextTool("ChatGPT");
            export.setContextUpdatedAt(LocalDateTime.now().minusDays(1));

            // --- Done --------------------------------------------------------
            Task pitch = new Task("Cut the pitch to five minutes", TaskType.FEATURE,
                    TaskStatus.DONE, "AI", today.minusDays(3));
            pitch.setDescription("Two dry runs with a timer; the first one ran to nine minutes and lost the ending.");

            Task seed = new Task("Seed a demo dataset", TaskType.FEATURE,
                    TaskStatus.DONE, "Yifan", today.minusDays(6));
            seed.setDescription("Eight tasks, two per column, every due-state represented once.");

            tasks.save(readme);
            tasks.save(drift);
            tasks.save(contextField);
            tasks.save(deploy);
            tasks.save(handle);
            tasks.save(export);
            tasks.save(pitch);
            tasks.save(seed);
        };
    }

    private static void seedAnchor(AnchorRepository anchors, AnchorKey key, String url) {
        if (anchors.findById(key).isPresent()) {
            return;
        }
        try {
            AnchorSlot slot = new AnchorSlot(key);
            slot.setUrl(url);
            slot.setUpdatedAt(LocalDateTime.now());
            anchors.save(slot);
        } catch (DataIntegrityViolationException alreadyCreatedByARequest) {
            // Fine: the request path created the same row a millisecond earlier.
        }
    }
}
