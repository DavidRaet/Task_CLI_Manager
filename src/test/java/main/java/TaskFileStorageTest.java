package main.java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskFileStorageTest {
    @TempDir
    Path directory;

    @Test
    void savesTasksAndLoadsThemBack() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        List<Task> tasks = List.of(new Task(Priority.HIGH, "Préparer release notes", false),
                new Task(Priority.LOW, "Tidy desk", true));
        storage.saveTask(tasks.get(0));
        storage.saveTask(tasks.get(1));

        assertEquals(List.of("HIGH | Préparer release notes | false", "LOW | Tidy desk | true"),
                Files.readAllLines(storage.path));
        assertEquals(tasks, load(storage));

    }

    @Test
    void loadsAndNormalizesEveryTask() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        Files.writeString(storage.path, "high | Prepare release notes | false\n LOW| Tidy desk | TRUE\n");

        assertEquals(List.of(new Task(Priority.HIGH, "Prepare release notes", false),
                new Task(Priority.LOW, "Tidy desk", true)), load(storage));

        Files.writeString(storage.path, "");
        assertEquals(List.of(), load(storage));
    }

    @Test
    void rejectsMalformedTasks() throws Exception {
        TaskFileStorage storage = new TaskFileStorage();
        storage.path = directory.resolve("tasks.txt");
        for (String line : List.of("high|Title", "high|Title|false|extra", "urgent|Title|false",
                "high|Title|", "high|Title|maybe", "high| |false")) {
            Files.writeString(storage.path, line);
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
