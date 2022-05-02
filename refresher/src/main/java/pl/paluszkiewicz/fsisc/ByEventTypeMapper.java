package pl.paluszkiewicz.fsisc;

import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

public class ByEventTypeMapper<S extends Secret> implements SecretMapper<S> {

    private final ByEventTypeMappingDelegate<S> delegate;

    public ByEventTypeMapper(ByEventTypeMappingDelegate<S> delegate) {
        this.delegate = delegate;
    }

    public static <S extends Secret> ByEventTypeMapper<S> nullOnDelete() {
        return new ByEventTypeMapper<>(new NullOnDelete<>());
    }

    @Override
    public char[] apply(WatchEventType type, S source) {
        return switch (type) {
            case CREATE -> delegate.onCreate(source);
            case EDIT -> delegate.onEdit(source);
            case DELETE -> delegate.onDelete(source);
        };
    }

    private static final class NullOnDelete<S extends Secret> implements ByEventTypeMappingDelegate<S> {

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
