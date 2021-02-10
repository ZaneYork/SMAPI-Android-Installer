package com.zane.smapiinstaller.utils;

import android.os.Build;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

/**
 * @author Zane
 */
public class MultiprocessingUtil {
    public final static ExecutorService EXECUTOR_SERVICE = getExecutorService();

    public static ExecutorService getExecutorService() {
        int processors = Runtime.getRuntime().availableProcessors();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new ForkJoinPool(processors, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
        }
        return new ThreadPoolExecutor(processors, processors,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().build());
    }

    public static <T> TaskBundle<T> newTaskBundle(Consumer<T> onResult) {
        LinkedBlockingQueue<T> futureResults = new LinkedBlockingQueue<>();
        AtomicLong taskCount = new AtomicLong(1);
        CountDownLatch doneLatch = new CountDownLatch(1);
        EXECUTOR_SERVICE.submit(() -> {
            while (true) {
                try {
                    T result = futureResults.poll(50, TimeUnit.MILLISECONDS);
                    if (result != null) {
                        onResult.accept(result);
                    } else {
                        if (taskCount.get() == 0) {
                            if (futureResults.isEmpty()) {
                                doneLatch.countDown();
                                return;
                            }
                        }
                    }
                } catch (InterruptedException ignored) {
                    doneLatch.countDown();
                    return;
                }
            }
        });
        return new TaskBundle<>(taskCount, doneLatch, futureResults);
    }

    @Data
    @AllArgsConstructor
    public static class TaskBundle<T> {
        private AtomicLong taskCount;
        private CountDownLatch doneLatch;
        private LinkedBlockingQueue<T> futureResults;

        public void submitTask(Supplier<T> task) {
            EXECUTOR_SERVICE.submit(() -> {
                taskCount.incrementAndGet();
                try {
                    futureResults.add(task.get());
                } finally {
                    taskCount.decrementAndGet();
                }
            });
        }

        @SneakyThrows
        public void join() {
            taskCount.decrementAndGet();
            doneLatch.await();
        }
    }
}
