package pl.paluszkiewicz.fsisc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import static org.slf4j.LoggerFactory.getLogger;
import static pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType.CREATE;
import static pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType.DELETE;
import static pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType.EDIT;

public class PollingJob implements Runnable {

    private static final Logger LOG = getLogger(PollingJob.class);

    private static final Map<Kind<Path>, WatchEventType> EVENT_TYPE_MAPPING = Map.of(
            ENTRY_CREATE, CREATE,
            ENTRY_DELETE, DELETE,
            ENTRY_MODIFY, EDIT
    );

    private final ConcurrentHashMap<Path, SecretChangedCallback<FileSecretSource>> subscriptions;
    private final WatchKey watchKey;
    private final Path root;

    public PollingJob(ConcurrentHashMap<Path, SecretChangedCallback<FileSecretSource>> subscriptions,
            WatchKey watchKey, Path root) {
        this.subscriptions = subscriptions;
        this.watchKey = watchKey;
        this.root = root;
    }

    @Override
    public void run() {
        watchKey.pollEvents().stream()
                .filter(this::isPathEvent)
                .map(this::toPathEvent)
                .filter(Objects::nonNull)
                .forEach(this::handlePathEvent);
    }

    private boolean isPathEvent(WatchEvent<?> watchEvent) {
        return watchEvent.context() instanceof Path;
    }

    @SuppressWarnings("unchecked") // isPathEvent is called before this method, so cast should be safe
    private PathEvent toPathEvent(WatchEvent<?> watchEvent) {
        Path eventPath = (Path) watchEvent.context();
        Kind<Path> kind = (Kind<Path>) watchEvent.kind();

        var consumer = subscriptions.get(eventPath);
        if (consumer == null) {
            return null;
        }

        Path path = root.resolve(eventPath);
        return new PathEvent(path, kind, consumer);
    }

    private void handlePathEvent(PathEvent event) {
        Optional.ofNullable(EVENT_TYPE_MAPPING.get(event.kind()))
                .ifPresentOrElse(
                        event::handleAs,
                        () -> LOG.error("Unsupported event type: {}", event.kind())
                );
    }

    private record PathEvent(Path path, Kind<Path> kind, BiConsumer<WatchEventType, FileSecretSource> consumer) {

        void handleAs(WatchEventType type) {
            FileSecretSource source = type == DELETE ? null : secretSource(path);
            consumer.accept(type, source);
        }

        private FileSecretSource secretSource(Path file) {
            try {
                var is = Files.newInputStream(file, StandardOpenOption.READ);
                return new FileSecretSource(is);
            } catch (IOException e) {
                throw new RuntimeException("Could not open input stream to path: " + file, e);
            }
        }
    }
}
