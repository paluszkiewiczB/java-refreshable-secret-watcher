package pl.paluszkiewicz.fsisc;

import java.util.Optional;

/*
* K - type of key of the secret
* S - type of source of the secret
* C - type of configuration for the system passed to method start
*/
public interface SecretSourceWatcher<K, S extends SecretSource, C> {

    WatchResult watch(K key, SecretChangedCallback<S> changesConsumer);

    boolean start(C config);

    enum WatchEventType {
        CREATE, EDIT, DELETE
    }

    interface WatchResult {

        Optional<String> error();

        default boolean isOk() {
            return error().isEmpty();
        }

        static WatchResult ok() {
            return Optional::empty;
        }

        static WatchResult fromError(String e) {
            return () -> Optional.ofNullable(e);
        }
    }
}
