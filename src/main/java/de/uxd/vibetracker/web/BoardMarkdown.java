package de.uxd.vibetracker.web;

import de.uxd.vibetracker.task.ColumnView;
import de.uxd.vibetracker.task.TaskStatus;
import de.uxd.vibetracker.task.TaskView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * The whole board as Markdown — a status update you can paste into a chat or a
 * commit message without retyping anything (extra feature, see PRD §9).
 */
public final class BoardMarkdown {

    private static final DateTimeFormatter DAY =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);

    private BoardMarkdown() {
    }

    public static String render(List<ColumnView> columns, LocalDate today) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# VibeTracker board — ").append(DAY.format(today)).append("\n\n");

        for (ColumnView column : columns) {
            markdown.append("## ").append(column.label())
                    .append(" (").append(column.getCount()).append(")\n\n");

            if (column.isEmpty()) {
                markdown.append("_Nothing here._\n\n");
                continue;
            }

            for (TaskView task : column.tasks()) {
                markdown.append("- [").append(task.status() == TaskStatus.DONE ? "x" : " ").append("] ");
                markdown.append("**").append(task.title()).append("** — ");
                markdown.append(task.type().getLabel().toLowerCase(Locale.ROOT));
                markdown.append(" · ").append(task.assignee());
                markdown.append(" · ").append(task.dueLabel());
                markdown.append('\n');
            }
            markdown.append('\n');
        }
        return markdown.toString();
    }
}
