package pl.paluszkiewicz.fsisc;

import java.util.function.BiConsumer;

import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

public interface SecretChangedCallback<S extends Secret> extends BiConsumer<WatchEventType, S> {

}
