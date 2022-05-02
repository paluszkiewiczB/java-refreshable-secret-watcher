package pl.paluszkiewicz.fsisc

import groovy.transform.AutoImplement
import pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType
import spock.lang.Specification

import static pl.paluszkiewicz.fsisc.SecretSourceWatcher.WatchEventType.CREATE

class ClosingCallbackSpec extends Specification {
    def 'should close secret after use'() {
        given:
            def secret = new CloseableSecret()
            def closing = new ClosingCallback(new Callback())
        when:
            closing.accept(CREATE, secret)
        then:
            secret.closed
    }

    def 'should delegate consuming the secret to delegate'() {
        given:
            def secret = new CloseableSecret()
            def callback = new Callback()
            def closing = new ClosingCallback(callback)
        when:
            closing.accept(CREATE, secret)
        then:
            callback.type == CREATE
            callback.closeableSecret == secret
    }

    @AutoImplement
    private static final class CloseableSecret implements Secret, AutoCloseable {
        private boolean closed = false

        @Override
        char[] secret() {
            return char[] { 'a' }
        }

        @Override
        void close() {
            closed = true
        }
    }

    private static final class Callback implements SecretChangedCallback<CloseableSecret> {
        private WatchEventType type
        private CloseableSecret closeableSecret

        @Override
        void accept(WatchEventType watchEventType, CloseableSecret closeableSecret) {
            this.type = watchEventType
            this.closeableSecret = closeableSecret
        }
    }
}
