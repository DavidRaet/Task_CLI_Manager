package main.java;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
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
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class TaskFileStorage {
    Path path = Paths.get("tasks.txt");
    Charset charset = StandardCharsets.UTF_8;

    public void saveTask(Task task) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, charset,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(formatTask(task));
                writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Task> loadTasks() {
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

    public void deleteTask(UUID id) {
        try {
            List<String> lines = Files.readAllLines(path);

            if (!lines.removeIf(line -> line.toLowerCase(Locale.ROOT).startsWith(id.toString()))){
                throw new IllegalArgumentException("No task found with ID: " + id);
            }


            Files.write(path, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to delete task " + id + " from " + path, e);
        }
    }

    private String formatTask(Task task) {
        return task.id() + " | " + task.priority() + " | " + task.title() + " | " + task.completed();
    }

    private Task parseTask(String line) {
        String[] attributes = line.split("\\|", -1);
        if (attributes.length != 4) {
            throw new IllegalArgumentException("Expected priority | title | completed: " + line);
        }
        UUID id = UUID.fromString(attributes[0].trim());
        Priority priority = Priority.valueOf(attributes[1].trim().toUpperCase(Locale.ROOT));
        String title = attributes[2].trim();
        String completed = attributes[3].trim();
        if (title.isEmpty() || !(completed.equalsIgnoreCase("true") || completed.equalsIgnoreCase("false"))) {
            throw new IllegalArgumentException("Invalid task title or completion status: " + line);
        }
        return new Task(id, priority, title, Boolean.parseBoolean(completed));
    }
}
