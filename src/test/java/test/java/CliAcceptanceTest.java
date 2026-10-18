package test.java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CliAcceptanceTest {
    private static final Pattern TASK_ID = Pattern.compile("ID:\\s*(\\S+)");

    @TempDir
    Path dataDirectory;

    @Test
    void addedTaskIsAvailableAfterRestart() throws Exception {
        CommandResult added = run("add", "HIGH", "Prepare release notes");
        assertEquals(0, added.exitCode(), added.output());
        assertTrue(added.output().contains("ID:"), added.output());

        CommandResult listed = run("list");
        assertEquals(0, listed.exitCode(), listed.output());
        assertTrue(listed.output().contains("Prepare release notes"), listed.output());
        assertTrue(listed.output().contains("HIGH"), listed.output());
    }

    @Test
    void listFiltersByStatusAndPriorityAndSortsByPriority() throws Exception {
        add("LOW", "Tidy desk");
        Thread.sleep(50);
        add("HIGH", "Ship release");
        Thread.sleep(50);
        add("MEDIUM", "Review pull request");
        String releaseId = taskId(run("list").output(), "Ship release");
        run("complete", releaseId);

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
    void taskCanBeCompletedAndDeletedByIdAndMissingIdIsReported() throws Exception {
        add("MEDIUM", "Update documentation");
        String id = taskId(run("list").output(), "Update documentation");

        CommandResult completed = run("complete", id);
        assertEquals(0, completed.exitCode(), completed.output());
        assertTrue(run("list", "--status", "completed").output().contains("Update documentation"));

        CommandResult deleted = run("delete", id);
        assertEquals(0, deleted.exitCode(), deleted.output());
        assertFalse(run("list").output().contains("Update documentation"));

        CommandResult missing = run("complete", "missing-id");
        assertTrue(missing.output().toLowerCase().contains("no task"), missing.output());
    }

    @Test
    void invalidPriorityDoesNotChangeSavedTasks() throws Exception {
        add("HIGH", "Keep this task");

        CommandResult invalid = run("add", "URGENT", "Invalid task");
        assertNotEquals(0, invalid.exitCode(), invalid.output());
        assertTrue(run("list").output().contains("Keep this task"));
        assertFalse(run("list").output().contains("Invalid task"));
    }

    private void add(String priority, String title) throws Exception {
        CommandResult result = run("add", priority, title);
        assertEquals(0, result.exitCode(), result.output());
    }

    private String taskId(String listing, String title) {
        for (String line : listing.lines().toList()) {
            if (line.contains(title)) {
                Matcher matcher = TASK_ID.matcher(line);
                assertTrue(matcher.find(), "Task listing must show an ID: " + line);
                return matcher.group(1);
            }
        }
        fail("Task not found in listing: " + title + "\n" + listing);
        return "";
    }

    private CommandResult run(String... arguments) throws IOException, InterruptedException {
        String classpath = Arrays.stream(System.getProperty("java.class.path").split(Pattern.quote(System.getProperty("path.separator"))))
                .map(entry -> Path.of(entry).toAbsolutePath().normalize().toString())
                .collect(Collectors.joining(System.getProperty("path.separator")));
        List<String> command = new ArrayList<>(List.of("java", "-cp", classpath,
                System.getProperty("taskmanager.mainClass", "Main")));
        command.addAll(Arrays.asList(arguments));
        Process process = new ProcessBuilder(command)
                .directory(dataDirectory.toFile())
                .redirectErrorStream(true)
                .start();
        if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("CLI did not finish within 10 seconds");
        }
        return new CommandResult(process.exitValue(), new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }

    private record CommandResult(int exitCode, String output) { }
}
