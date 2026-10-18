package main.java;

import java.time.Instant;


public record Task(Priority priority, String title, boolean completed) {
    public Task(Priority priority, String title, boolean completed){
        this.title = title;
        this.priority = priority;
        this.completed = completed;
    }
}
