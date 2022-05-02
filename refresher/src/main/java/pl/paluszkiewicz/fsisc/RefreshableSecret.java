package pl.paluszkiewicz.fsisc;

import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType;

public class RefreshableSecret<S extends Secret> implements SecretChangedCallback<S>, Secret {

    private final ByEventTypeSecretMapper<S> sourceMapper;
    private final GuardedSecret secret;

    public static <S extends Secret> RefreshableSecret<S> nullOnDelete() {
        return new RefreshableSecret<>(ByEventTypeSecretMapper.nullOnDelete());
    }

    public static <S extends Secret> RefreshableSecret<S> nullOnDelete(char[] initial) {
        return new RefreshableSecret<>(ByEventTypeSecretMapper.nullOnDelete(), initial);
    }

    public RefreshableSecret(ByEventTypeSecretMapper<S> mapper) {
        this.sourceMapper = mapper;
        this.secret = new GuardedSecret();
    }

    public RefreshableSecret(ByEventTypeSecretMapper<S> mapper, char[] initial) {
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
