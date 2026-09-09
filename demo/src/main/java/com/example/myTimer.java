package com.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class myTimer {
    public static void startTimer() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> run(), 0, 1, TimeUnit.MILLISECONDS);
    }

    private static void run() {
        Events.get().tick();
    }
}
