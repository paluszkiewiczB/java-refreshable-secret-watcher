package pl.paluszkiewicz.fsisc;

import java.nio.file.Path;

record FileSecretPath(Path path) implements SecretPath {

    static FileSecretPath of(String path) {
        return new FileSecretPath(Path.of(path));
    }
}