package main.java;

import java.util.UUID;


public record Task(UUID id, Priority priority, String title, boolean completed) {
    public Task(UUID id, Priority priority, String title, boolean completed){
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.completed = completed;
    }
}
