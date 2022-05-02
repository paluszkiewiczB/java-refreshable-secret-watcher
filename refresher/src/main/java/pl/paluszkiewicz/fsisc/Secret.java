package pl.paluszkiewicz.fsisc;

public interface Secret {

    char[] secret();

    static Secret of(String secret) {
        return new StaticSecret(secret);
    }

    static Secret of(char[] secret) {
        return new StaticSecret(secret);
    }
}
