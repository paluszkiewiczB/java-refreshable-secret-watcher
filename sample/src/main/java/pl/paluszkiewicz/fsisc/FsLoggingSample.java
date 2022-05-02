package pl.paluszkiewicz.fsisc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.slf4j.LoggerFactory.getLogger;

public class FsLoggingSample {

    private static final Logger LOG = getLogger(FsLoggingSample.class);

    public static void main(String[] args) throws IOException {
        Path root = Path.of("secrets");
        Path example = Path.of("example.txt");
        FileSystemWatcher fsWatcher = FileSystemWatcher.defaultWatcher(root);
        RefreshableSecret<FileSecret> secret = RefreshableSecret.nullOnDelete();
        var result = fsWatcher.watch(new FileSecretPath(example), secret);
        if (!result.isOk()) {
            String error = result.error().map(e -> "Error: " + e).orElse("No error specified");
            LOG.error("Could not watch path: " + example + ". Error was: " + error);
            return;
        }

        new SecretConsumer(secret);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        FsStartConfig startConfig = new FsStartConfig(executorService);
        try {
            fsWatcher.start(startConfig);
        } catch (Exception e) {
            LOG.error("Error", e);
        }
    }

    private static final class SecretConsumer {

        private static final Logger LOG = getLogger(SecretConsumer.class);

        private final Secret secret;

        SecretConsumer(Secret secret) {
            this.secret = secret;
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::logSecret, 1, 1, SECONDS);
        }

        public void logSecret() {
            var secret = this.secret.secret();
            if (secret != null) {
                String value = new String(secret);
                LOG.info("Value of secret is: {}", value);
            } else {
                LOG.info("secret is null!");
            }
        }

    }
}
