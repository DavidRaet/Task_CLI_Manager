package main.java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;

class TaskFileStorageTest {
    @TempDir
    Path directory;

    @Test
    void deletesOnlyTheMatchingTask() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        Task removed = new Task(UUID.randomUUID(), Priority.HIGH, "Remove", false);
        Task retained = new Task(UUID.randomUUID(), Priority.LOW, "Keep", true);
        storage.saveTask(removed);
        storage.saveTask(retained);

        storage.deleteTask(removed.id());

        assertEquals(List.of(retained), storage.loadTasks());
    }

    @Test
    void reportsUnknownIdWithoutChangingTheFile() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        storage.saveTask(new Task(UUID.randomUUID(), Priority.LOW, "Keep", false));
        String original = Files.readString(storage.path);
        UUID missingId = UUID.randomUUID();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> storage.deleteTask(missingId));

        assertTrue(error.getMessage().contains(missingId.toString()));
        assertEquals(original, Files.readString(storage.path));
    }

    @Test
    void reportsDeletionIoErrors() {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("missing.txt");
        UUID id = UUID.randomUUID();

        UncheckedIOException error = assertThrows(UncheckedIOException.class,
                () -> storage.deleteTask(id));

        assertTrue(error.getMessage().contains("Unable to delete task " + id));
        assertNotNull(error.getCause());
    }

    @Test
    void savesTasksAndLoadsThemBack() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        List<Task> tasks = List.of(new Task(firstId, Priority.HIGH, "Préparer release notes", false),
                new Task(secondId, Priority.LOW, "Tidy desk", true));
        storage.saveTask(tasks.get(0));
        storage.saveTask(tasks.get(1));

        assertEquals(List.of(firstId + " | HIGH | Préparer release notes | false",
                        secondId + " | LOW | Tidy desk | true"),
                Files.readAllLines(storage.path));
        assertEquals(tasks, load(storage));

    }

    @Test
    void loadsAndNormalizesEveryTask() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Files.writeString(storage.path, firstId + " | high | Prepare release notes | false\n "
                + secondId + " | LOW| Tidy desk | TRUE\n");

        assertEquals(List.of(new Task(firstId, Priority.HIGH, "Prepare release notes", false),
                new Task(secondId, Priority.LOW, "Tidy desk", true)), load(storage));

        Files.writeString(storage.path, "");
        assertEquals(List.of(), load(storage));
    }

    @Test
    void rejectsMalformedTasks() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        UUID id = UUID.randomUUID();
        for (String line : List.of("high|Title", "high|Title|false|extra", "urgent|Title|false",
                "high|Title|", "high|Title|maybe", "high| |false")) {
            Files.writeString(storage.path, id + " | " + line);
            InvocationTargetException error = assertThrows(InvocationTargetException.class, () -> load(storage));
            assertInstanceOf(IllegalArgumentException.class, error.getCause());
        }
    }

    private Object load(TaskFileStorage storage) throws Exception {
        Method method = TaskFileStorage.class.getDeclaredMethod("loadTasks");
        method.setAccessible(true);
        return method.invoke(storage);
    }
}
