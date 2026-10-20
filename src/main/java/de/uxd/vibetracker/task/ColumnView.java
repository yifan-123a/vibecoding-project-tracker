package de.uxd.vibetracker.task;

import java.util.List;

/** One Kanban column, with its cards already filtered and sorted. */
public record ColumnView(String id, String label, List<TaskView> tasks) {

    public int getCount() {
        return tasks.size();
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
