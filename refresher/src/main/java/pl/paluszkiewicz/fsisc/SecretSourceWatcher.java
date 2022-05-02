package pl.paluszkiewicz.fsisc;

import java.util.Optional;

public interface SecretSourceWatcher<P extends SecretPath, S extends SecretSource, C extends StartConfig> {

    WatchResult watch(P path, SecretChangedCallback<S> changesConsumer);

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
