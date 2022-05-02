package pl.paluszkiewicz.fsisc.simple;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Simple {

    public static void main(String[] args) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path secrets = Path.of("secrets");
        WatchKey key = secrets.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        var exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> poll(key), 1, 1, SECONDS);
    }

    private static void poll(WatchKey key) {
        key.pollEvents().forEach(we -> {
            if (we.context() instanceof Path p) {
                if (p.equals(Path.of("example.txt"))) {
                    System.out.println("simple.txt has changed! Event kind: " + we.kind().name());
                }
            } else {
                System.err.println("Unsupported action context type!");
            }
            key.reset();
        });
    }
}
