package pl.paluszkiewicz.fsisc;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

public record PollingConfig(Duration initialDelay, Duration period) {

    public static final PollingConfig DEFAULT = new PollingConfig(ofSeconds(1), ofSeconds(1));

    public long initialDelayMillis() {
        return initialDelay.toMillis();
    }

    public long periodMillis() {
        return period.toMillis();
    }
}
