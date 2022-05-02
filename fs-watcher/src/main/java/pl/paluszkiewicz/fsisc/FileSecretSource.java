package pl.paluszkiewicz.fsisc;

import java.io.IOException;
import java.io.InputStream;

record FileSecretSource(InputStream inputStream) implements SecretSource, AutoCloseable {

    @Override
    public char[] secret() {
        try {
            // intermediate string should not end up in String pool, because constructor is used?
            return new String(inputStream.readAllBytes()).toCharArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
