package pl.paluszkiewicz.fsisc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GuardedSecret implements Secret {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private char[] value;

    public GuardedSecret() {
        this.value = null;
    }

    public GuardedSecret(char[] value) {
        this.value = value;
    }

    public void write(char[] newValue) {
        Lock lock = this.lock.writeLock();
        try {
            lock.lock();
            this.value = copy(newValue);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public char[] secret() {
        Lock lock = this.lock.readLock();
        try {
            lock.lock();
            char[] toReturn = copy(value);
        } finally {
            lock.unlock();    
        }
        return toReturn;
    }

    // copying is done to prevent modification of the secret by other reference
    private static char[] copy(char[] source) {
        if (source == null) {
            return null;
        }
        char[] toReturn = new char[source.length];
        System.arraycopy(source, 0, toReturn, 0, source.length);
        return toReturn;
    }
}
