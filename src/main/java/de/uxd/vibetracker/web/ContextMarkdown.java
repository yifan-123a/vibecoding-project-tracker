package de.uxd.vibetracker.web;

import de.uxd.vibetracker.task.Task;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Task → paste-ready Markdown (M10).
 *
 * This is the product's whole promise in one method: the thing you copy has to
 * read like a briefing a colleague wrote, not like a database dump. So: a title,
 * one metadata line, the description as prose, then the context verbatim.
 */
public final class ContextMarkdown {

    private static final DateTimeFormatter DUE_DATE =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);

    private ContextMarkdown() {
    }

    public static String render(Task task) {
        StringBuilder markdown = new StringBuilder();

        markdown.append("# ").append(task.getTitle()).append("\n\n");
        markdown.append("**").append(metaLine(task)).append("**\n\n");

        if (hasText(task.getDescription())) {
            markdown.append(task.getDescription().trim()).append("\n\n");
        }

        markdown.append("## Context");
        String updated = TimeAgo.format(task.getContextUpdatedAt());
        if (updated != null) {
            markdown.append(" (updated ").append(updated);
            if (task.getContextTool() != null) {
                markdown.append(", written for ").append(task.getContextTool());
            }
            markdown.append(")");
        } else if (task.getContextTool() != null) {
            markdown.append(" (written for ").append(task.getContextTool()).append(")");
        }
        markdown.append("\n\n");

        if (hasText(task.getContext())) {
            markdown.append(task.getContext().trim()).append("\n");
        } else {
            markdown.append("_No context written yet._\n");
        }

        return markdown.toString();
    }

    private static String metaLine(Task task) {
        StringBuilder meta = new StringBuilder();
        meta.append(task.getType().getLabel().toLowerCase(Locale.ROOT));
        meta.append(" · ").append(task.getStatus().getLabel());
        meta.append(" · ").append(task.getAssignee());
        if (task.getDueDate() != null) {
            meta.append(" · due ").append(DUE_DATE.format(task.getDueDate()));
        }
        return meta.toString();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
