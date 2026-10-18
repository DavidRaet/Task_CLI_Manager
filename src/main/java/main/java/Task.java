package main.java;

import java.time.Instant;


public record Task(String id, String title, Priority priority, boolean completed, Instant createdAt) {
    public Task(String id, String title, Priority priority, boolean completed, Instant createdAt){
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.completed = completed;
        this.createdAt = createdAt;
    }
}
