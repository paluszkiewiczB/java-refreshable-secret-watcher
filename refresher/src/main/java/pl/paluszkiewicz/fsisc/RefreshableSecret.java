package pl.paluszkiewicz.fsisc;

import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

public class RefreshableSecret<S extends Secret> implements SecretChangedCallback<S>, Secret {

    private final SecretMapper<S> sourceMapper;
    private final GuardedSecret secret;

    public static <S extends Secret> RefreshableSecret<S> nullOnDelete() {
        return new RefreshableSecret<>(ByEventTypeMapper.nullOnDelete());
    }

    public static <S extends Secret> RefreshableSecret<S> nullOnDelete(char[] initial) {
        return new RefreshableSecret<>(ByEventTypeMapper.nullOnDelete(), initial);
    }

    public RefreshableSecret(SecretMapper<S> mapper) {
        this.sourceMapper = mapper;
        this.secret = new GuardedSecret();
    }

    public RefreshableSecret(SecretMapper<S> mapper, char[] initial) {
        this.sourceMapper = mapper;
        this.secret = new GuardedSecret(initial);
    }

    @Override
    public void accept(WatchEventType watchEventType, S s) {
        char[] newValue = sourceMapper.apply(watchEventType, s);
        secret.write(newValue);
    }

    @Override
    public char[] secret() {
        return secret.secret();
    }
}
