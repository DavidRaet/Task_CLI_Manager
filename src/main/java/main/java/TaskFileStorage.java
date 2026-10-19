package main.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TaskFileStorage {
    Path path = Paths.get("tasks.txt");

    private void saveTasks(List<Task> tasks) throws IOException {

    }

    private List<Task> loadTasks() {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<Task> tasks = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                tasks.add(parseTask(line));
            }
            return tasks;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load tasks from " + path, e);
        }
    }

    private Task parseTask(String line) {
        String[] attributes = line.split("\\|", -1);
        if (attributes.length != 3) {
            throw new IllegalArgumentException("Expected priority | title | completed: " + line);
        }
        Priority priority = Priority.valueOf(attributes[0].trim().toUpperCase(Locale.ROOT));
        String title = attributes[1].trim();
        String completed = attributes[2].trim();
        if (title.isEmpty() || !(completed.equalsIgnoreCase("true") || completed.equalsIgnoreCase("false"))) {
            throw new IllegalArgumentException("Invalid task title or completion status: " + line);
        }
        return new Task(priority, title, Boolean.parseBoolean(completed));
    }
}
