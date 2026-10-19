package main.java;

//A task stores an ID, title, priority, completion status, and creation time. Model the task as a record and priority as an enum with at least three levels.
//From the CLI, a user can add a task, list tasks, mark a task complete by ID, and delete a task by ID.
//Listing can filter by completion status and priority, and sort by priority or creation time. Filtering and sorting operate on the in-memory collection using streams.
//Looking up a task by ID handles a missing task safely with Optional: the command reports that no matching task exists and does not crash.
//Tasks are saved to a local file. On startup, the app loads saved tasks; after a change, it saves them so they remain available after restart.
//Invalid input (such as an unknown priority or malformed ID) produces a clear message and leaves saved data intact.


//add <PRIORITY> <TITLE> creates a task. Priorities are LOW, MEDIUM, and HIGH; output includes ID: <id>.
//list accepts --status open|completed, --priority LOW|MEDIUM|HIGH, and --sort priority|created.
//Priority sorting puts HIGH before MEDIUM before LOW; creation sorting puts older tasks first.
//complete <ID> and delete <ID> act on one task. An unknown ID reports that no task matched.
//Run one command per process. The app reads and writes its task file in the process working directory, so a fresh process can reload it.


import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    Map<UUID, Task> tasks = new LinkedHashMap<>();
//  create a function to normalize tasks from the .txt file into Task Objects and append them to the LinkedHashMap
    public static void main(String[] args) {
        Main manager = new Main();
        Scanner scanner = new Scanner(System.in);
        System.out.println("CLI Task Manager. Type exit to leave the program.");

        while(true) {
            System.out.println("Commands: add, list, complete, delete");
            System.out.print("> ");
            String command = scanner.nextLine().strip().toLowerCase(Locale.ROOT);
            switch (command) {
                case "exit" -> {
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                }

                case "add" -> {
                    System.out.print("Priority: ");
                    String priority = scanner.nextLine();

                    System.out.print("Title: ");
                    String title = scanner.nextLine();

                    try {
                        UUID taskId = manager.addTask(priority, title);
                        System.out.printf("Task Added: %s (Priority: %s, Id: %s) ", title, priority, taskId);
                        System.out.println();
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case "list" -> manager.listTasks();
                case "delete" -> {
                    System.out.println("Paste the id of the task to remove.");
                    System.out.print("> ");
                    String id = scanner.nextLine().strip();
                    UUID convertedStringId = UUID.fromString(id);
                    manager.deleteTask(convertedStringId);
                    System.out.printf("Task %s removed", id);
                    System.out.println();
                }
                case "complete" -> {
                    System.out.println("Paste the id of the task to complete.");
                    System.out.print("> ");
                    String id = scanner.nextLine().strip();
                    UUID convertedStringId = UUID.fromString(id);
                    manager.markTaskAsCompleted(convertedStringId);
                    System.out.printf("Task %s completed", id);
                    System.out.println();
                }
                default -> {
                    System.out.println("Invalid command.");
                    System.out.println();
                    System.out.print("> ");
                }
            }
        }

    }
    public Priority matchPriority(String priority){
        return switch (priority.strip().toLowerCase(Locale.ROOT)) {
            case "low" -> Priority.LOW;
            case "medium" -> Priority.MEDIUM;
            case "high" -> Priority.HIGH;
            default -> throw new IllegalArgumentException("Invalid priority.");
        };
    }

    public void listTasks(){
        for (Map.Entry<UUID, Task> entry : tasks.entrySet()){
            System.out.println(entry.getValue());
        }
    }

    public UUID addTask(String priority, String title) {
        UUID id = UUID.randomUUID();
        Priority pri = matchPriority(priority);
        tasks.put(id, new Task(pri, title, false));
//      write to file and add bars to separate them
        return id;
    }

    public void deleteTask(UUID id){
        tasks.remove(id);
    }

    public void markTaskAsCompleted(UUID id){
        Task oldTask = tasks.get(id);
        Task updatedTask = new Task(oldTask.priority(), oldTask.title(), true);
        tasks.put(id, updatedTask);
    }
}
