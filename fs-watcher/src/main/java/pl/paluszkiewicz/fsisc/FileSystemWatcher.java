package pl.paluszkiewicz.fsisc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileSystemWatcher implements SecretSourceWatcher<Path, FileSecret, FsStartConfig>,
        Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemWatcher.class);

    private final ConcurrentHashMap<Path, SecretChangedCallback<FileSecret>> subscriptions = new ConcurrentHashMap<>();
    private final WatchService watchService;
    private final Path root;

    public FileSystemWatcher(WatchService watchService, Path root) {
        this.watchService = watchService;
        this.root = root;
    }

    public static FileSystemWatcher defaultWatcher(Path root) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        return new FileSystemWatcher(watchService, root);
    }

    @Override
    public WatchResult watch(Path key, SecretChangedCallback<FileSecret> callback) {
        subscriptions.put(key, new ClosingCallback<>(callback));
        LOG.debug("Registered watch for file: {}", key);
        return WatchResult.ok();
    }

    private boolean yieldInitialValues() {
        return subscriptions.entrySet().stream()
                .filter(e -> Files.exists(e.getKey()))
                .map(e -> tryToYield(root.resolve(e.getKey()), e.getValue()))
                .flatMap(Optional::stream)
                .peek(yr -> LOG.debug("Exception when yielding initial value for path: {}", yr.path, yr.throwable()))
                .count() != 0;
    }

    private Optional<YieldingError> tryToYield(Path path, SecretChangedCallback<FileSecret> callback) {
        try {
            InputStream is = Files.newInputStream(path);
            callback.accept(WatchEventType.CREATE, new FileSecret(is));
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(new YieldingError(path, e));
        }
    }

    @Override
    public boolean start(FsStartConfig config) {
        boolean yieldSuccess = yieldInitialValues();
        if (yieldSuccess) {
            LOG.info("Initial values yielded successfully");
        } else {
            LOG.error("Error yielding initial values");
        }

        LOG.info("Watching directory: {}", root.toAbsolutePath());
        WatchRootResult watchRootResult = tryToWatchRoot();
        if (watchRootResult.exception() != null) {
            LOG.error("Could not start watching root: {}, error was: ", root, watchRootResult.exception);
            return false;
        }
        WatchKey watchKey = watchRootResult.key();
        PollingJob command = new PollingJob(subscriptions, watchKey, root);
        var polling = config.pollingConfig();
        config.executorService()
                .scheduleAtFixedRate(command, polling.initialDelayMillis(), polling.periodMillis(), MILLISECONDS);
        return true;
    }

    @Override
    public void close() throws IOException {
        watchService.close();
    }

    WatchRootResult tryToWatchRoot() {
        Kind[] events = new Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
        try {
            WatchKey watchKey = root.register(watchService, events, SensitivityWatchEventModifier.HIGH);
            return new WatchRootResult(watchKey, null);
        } catch (IOException e) {
            return new WatchRootResult(null, e);
        }
    }

    private record WatchRootResult(WatchKey key, Throwable exception) {

    }

    private record YieldingError(Path path, Throwable throwable) {

    }
}
