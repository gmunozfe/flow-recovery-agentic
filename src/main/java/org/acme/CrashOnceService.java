package org.acme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CrashOnceService {

    private static final Path MARKER = Path.of("target", "agentic-crash-once.marker");

    public boolean shouldCrash(String orderId) {
        return orderId != null
                && orderId.startsWith("crash-")
                && Files.notExists(MARKER);
    }

    public void markCrashed() {
        try {
            Files.createDirectories(MARKER.getParent());
            Files.writeString(MARKER, "crashed");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create crash marker", e);
        }
    }

    public void reset() {
        try {
            Files.deleteIfExists(MARKER);
        } catch (IOException e) {
            throw new RuntimeException("Failed to reset crash marker", e);
        }
    }
}
