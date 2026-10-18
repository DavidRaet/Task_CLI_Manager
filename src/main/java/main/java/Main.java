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

public class Main {
    public static void main(String[] args) {
        System.out.println("CLI Task Manager");
        System.out.println("Commands: add, list, complete, delete");

//        if user begins the command with add, they will need to input the title of the task and priority (LOW, MEDIUM, HIGH)
//        if the user lists an invalid command, they will be met with an error dependent on how the test case
    }

    public void listTasks(){

    }

    public void addTask(){

    }

    public void deleteTask(){

    }

    public void completeTask(){

    }

    public void deleteTaskById(){

    }
}
