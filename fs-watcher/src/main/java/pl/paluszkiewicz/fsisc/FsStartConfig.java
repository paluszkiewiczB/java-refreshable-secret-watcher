package pl.paluszkiewicz.fsisc;

import java.util.concurrent.ScheduledExecutorService;

public record FsStartConfig(ScheduledExecutorService executorService, PollingConfig pollingConfig) {

    public FsStartConfig(ScheduledExecutorService executorService) {
        this(executorService, PollingConfig.DEFAULT);
    }
}
