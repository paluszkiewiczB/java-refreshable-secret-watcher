package pl.paluszkiewicz.fsisc;

import java.util.function.BiFunction;

import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

public abstract class ByEventTypeSecretMapper<S extends Secret> implements BiFunction<WatchEventType, S, char[]> {

    public static <S extends Secret> ByEventTypeSecretMapper<S> nullOnDelete() {
        return new NullOnDelete<>();
    }

    public abstract char[] onCreate(S source);

    public abstract char[] onEdit(S source);

    public abstract char[] onDelete(S source);

    @Override
    public char[] apply(WatchEventType type, S source) {
        return switch (type) {
            case CREATE -> onCreate(source);
            case EDIT -> onEdit(source);
            case DELETE -> onDelete(source);
        };
    }

    private static final class NullOnDelete<S extends Secret> extends ByEventTypeSecretMapper<S> {

        @Override
        public char[] onCreate(S source) {
            return source.secret();
        }

        @Override
        public char[] onEdit(S source) {
            return source.secret();
        }

        @Override
        public char[] onDelete(S source) {
            return null;
        }
    }
}
