package test.java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CliAcceptanceTest {
    private static final Pattern TASK_ID = Pattern.compile("Id:\\s*([0-9a-f-]{36})");

    @TempDir
    Path dataDirectory;

    @Test
    void addedTaskIsAvailableAfterRestart() throws Exception {
        CommandResult added = run("add", "HIGH", "Prepare release notes");
        assertEquals(0, added.exitCode(), added.output());
        assertTrue(TASK_ID.matcher(added.output()).find(), added.output());

        CommandResult listed = run("list");
        assertEquals(0, listed.exitCode(), listed.output());
        assertTrue(listed.output().contains("Prepare release notes"), listed.output());
        assertTrue(listed.output().contains("HIGH"), listed.output());
    }

    @Test
    void listFiltersByStatusAndPriorityAndSortsByPriority() throws Exception {
        Files.writeString(dataDirectory.resolve("tasks.txt"),
                "LOW | Tidy desk | false\nHIGH | Ship release | true\nMEDIUM | Review pull request | false\n");
        // Filters and sorting are not implemented yet; keep these expectations active.
        CommandResult completed = run("list", "--status", "completed");
        assertTrue(completed.output().contains("Ship release"), completed.output());
        assertFalse(completed.output().contains("Tidy desk"), completed.output());

        CommandResult highPriority = run("list", "--priority", "HIGH");
        assertTrue(highPriority.output().contains("Ship release"), highPriority.output());
        assertFalse(highPriority.output().contains("Review pull request"), highPriority.output());

        CommandResult sorted = run("list", "--sort", "priority");
        assertTrue(sorted.output().indexOf("Ship release") < sorted.output().indexOf("Review pull request"), sorted.output());
        assertTrue(sorted.output().indexOf("Review pull request") < sorted.output().indexOf("Tidy desk"), sorted.output());

        CommandResult created = run("list", "--sort", "created");
        assertTrue(created.output().indexOf("Tidy desk") < created.output().indexOf("Ship release"), created.output());
        assertTrue(created.output().indexOf("Ship release") < created.output().indexOf("Review pull request"), created.output());
    }

    @Test
    void taskCanBeCompletedByIdAfterRestart() throws Exception {
        String id = add("MEDIUM", "Update documentation");

        CommandResult completed = run("complete", id);
        assertEquals(0, completed.exitCode(), completed.output());
        assertTrue(run("list").output().contains("Task[priority=MEDIUM, title=Update documentation, completed=true]"));
    }

    @Test
    void taskCanBeDeletedByIdAfterRestart() throws Exception {
        String id = add("MEDIUM", "Update documentation");
        CommandResult deleted = run("delete", id);
        assertEquals(0, deleted.exitCode(), deleted.output());
        assertFalse(run("list").output().contains("Update documentation"));
    }

    @Test
    void missingIdIsReported() throws Exception {
        CommandResult missing = run("complete", "00000000-0000-0000-0000-000000000000");
        assertEquals(0, missing.exitCode(), missing.output());
        assertTrue(missing.output().toLowerCase().contains("no task"), missing.output());
    }

    @Test
    void invalidPriorityDoesNotChangeSavedTasks() throws Exception {
        add("HIGH", "Keep this task");

        CommandResult invalid = run("add", "URGENT", "Invalid task");
        assertEquals(0, invalid.exitCode(), invalid.output());
        assertTrue(invalid.output().contains("Invalid priority."), invalid.output());
        assertTrue(run("list").output().contains("Keep this task"));
        assertFalse(run("list").output().contains("Invalid task"));
    }

    private String add(String priority, String title) throws Exception {
        CommandResult result = run("add", priority, title);
        assertEquals(0, result.exitCode(), result.output());
        return taskId(result.output(), title);
    }

    private String taskId(String listing, String title) {
        for (String line : listing.lines().toList()) {
            if (line.contains(title)) {
                Matcher matcher = TASK_ID.matcher(line);
                assertTrue(matcher.find(), "Add confirmation must show an ID: " + line);
                return matcher.group(1);
            }
        }
        fail("Task not found in add confirmation: " + title + "\n" + listing);
        return "";
    }

    private CommandResult run(String... arguments) throws IOException, InterruptedException {
        String classpath = Arrays.stream(System.getProperty("java.class.path").split(Pattern.quote(System.getProperty("path.separator"))))
                .map(entry -> Path.of(entry).toAbsolutePath().normalize().toString())
                .collect(Collectors.joining(System.getProperty("path.separator")));
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", classpath,
                System.getProperty("taskmanager.mainClass", "main.java.Main")));
        Process process = new ProcessBuilder(command)
                .directory(dataDirectory.toFile())
                .redirectErrorStream(true)
                .start();
        try (var input = process.outputWriter(StandardCharsets.UTF_8)) {
            // Commands and prompted values each occupy a line in the interactive CLI.
            // Keep list options together so unsupported filters fail their assertions.
            String inputText = arguments[0].equals("list")
                    ? String.join(" ", arguments)
                    : String.join("\n", arguments);
            input.write(inputText + "\nexit\n");
        }
        if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("CLI did not finish within 10 seconds");
        }
        return new CommandResult(process.exitValue(), new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    private record CommandResult(int exitCode, String output) { }
}
