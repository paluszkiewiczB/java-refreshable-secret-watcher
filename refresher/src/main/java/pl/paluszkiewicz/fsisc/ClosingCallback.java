package pl.paluszkiewicz.fsisc;

import org.slf4j.Logger;
import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

import static org.slf4j.LoggerFactory.getLogger;

public class ClosingCallback<S extends SecretSource & AutoCloseable> implements SecretChangedCallback<S> {

    private static final Logger LOG = getLogger(ClosingCallback.class);

    private final SecretChangedCallback<S> delegate;

    public ClosingCallback(SecretChangedCallback<S> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void accept(WatchEventType watchEventType, S s) {
        delegate.accept(watchEventType, s);
        try {
            s.close();
        } catch (Exception e) {
            LOG.error("Could not close secret source: {}", s);
        }
    }
}
