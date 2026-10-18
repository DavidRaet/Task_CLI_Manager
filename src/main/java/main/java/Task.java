package main.java;

import java.time.Instant;


public record Task(String id, String title, Priority priority, boolean completed, Instant createdAt) {
}
