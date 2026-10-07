package com.example.coffeeordersystem.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;

public final class ConcurrencyTestSupport {

    private ConcurrencyTestSupport() {
    }

    public static List<Throwable> runConcurrently(int threadCount, IntConsumer task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executor.submit(() -> {
                try {
                    start.await();
                    task.accept(index);
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdown();
        return failures;
    }
}