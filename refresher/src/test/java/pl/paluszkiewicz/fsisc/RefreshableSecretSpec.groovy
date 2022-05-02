package pl.paluszkiewicz.fsisc

import spock.lang.Specification

import static pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType.CREATE
import static pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType.DELETE

class RefreshableSecretSpec extends Specification {

    def 'should set secret value on CREATE and set null on DELETE'() {
        given:
            def secret = RefreshableSecret.nullOnDelete()
        when:
            secret.accept(CREATE, Secret.of("value"))
        then:
            "value" == new String(secret.secret())
        when:
            secret.accept(DELETE, null)
        then:
            secret.secret() == null
    }
}
