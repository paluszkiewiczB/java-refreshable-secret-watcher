package pl.paluszkiewicz.fsisc;

public class StringSecret implements Secret {
    private final String secret;

    public StringSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public char[] secret() {
        return secret.toCharArray();
    }
}
