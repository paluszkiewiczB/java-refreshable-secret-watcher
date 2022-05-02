package pl.paluszkiewicz.fsisc;

class StaticSecret implements Secret {

    private final String secret;

    public StaticSecret(String secret) {
        this.secret = secret;
    }

    public StaticSecret(char[] secret) {
        this.secret = new String(secret);
    }

    @Override
    public char[] secret() {
        return secret.toCharArray();
    }
}
