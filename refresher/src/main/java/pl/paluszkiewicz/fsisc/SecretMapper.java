package pl.paluszkiewicz.fsisc;

import java.util.function.BiFunction;

import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

public interface SecretMapper<S extends Secret> extends BiFunction<WatchEventType, S, char[]> {

}
