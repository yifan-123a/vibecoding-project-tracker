package de.uxd.vibetracker.seed;

import de.uxd.vibetracker.settings.Settings;
import de.uxd.vibetracker.settings.SettingsRepository;
import de.uxd.vibetracker.task.Task;
import de.uxd.vibetracker.task.TaskRepository;
import de.uxd.vibetracker.task.TaskStatus;
import de.uxd.vibetracker.task.TaskType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Demo data, so the board is never empty on a fresh database (FR-3 / M13).
 * Dates are relative to today — the board always looks alive.
 */
@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seed(TaskRepository tasks, SettingsRepository settings) {
        return args -> {
            if (settings.findById(1L).isEmpty()) {
                settings.save(Settings.defaults());
            }
            if (tasks.count() > 0) {
                return;
            }

            LocalDate today = LocalDate.now();

            Task auth = new Task("Implement User Authentication", TaskType.FEATURE,
                    TaskStatus.TODO, "Yifan", today.plusDays(3));
            auth.setDescription("Email + password sign-in, session persistence, and a friendly error for a wrong passcode.");

            Task docs = new Task("Write API Documentation", TaskType.BUG, TaskStatus.TODO, "AI", today.plusDays(6));
            docs.setDescription("Endpoint reference is out of date after the rename; the examples still use the old field names.");

            Task schema = new Task("Design Database Schema", TaskType.FEATURE, TaskStatus.IN_PROGRESS,
                    "Yifan", today);
            schema.setDescription("Four entities, one join table. Deciding whether context lives on the task or beside it.");

            Task ci = new Task("Set Up CI/CD Pipeline", TaskType.FEATURE, TaskStatus.IN_PROGRESS,
                    "AI", today.minusDays(1));
            ci.setDescription("Build on every push, deploy the JAR to the host, keep the H2 file out of the artefact.");

            Task header = new Task("Fix Header Alignment Bug", TaskType.BUG, TaskStatus.REVIEW,
                    "Yifan", today.plusDays(1));
            header.setDescription("The tagline drifts 2 px on 1280 px screens because the wordmark uses a different baseline.");

            Task queries = new Task("Optimize Database Queries", TaskType.FEATURE, TaskStatus.DONE,
                    "AI", today.minusDays(5));
            queries.setDescription("Board load went from 14 queries to 3 by fetching tasks once and grouping in memory.");

            tasks.save(auth);
            tasks.save(docs);
            tasks.save(schema);
            tasks.save(ci);
            tasks.save(header);
            tasks.save(queries);
        };
    }
}
