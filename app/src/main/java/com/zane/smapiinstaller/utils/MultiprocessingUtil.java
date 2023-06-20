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

/**
 * @author Zane
 */
public class MultiprocessingUtil {
    public static final ExecutorService EXECUTOR_SERVICE = getExecutorService();

    public static ExecutorService getExecutorService() {
        int processors = Runtime.getRuntime().availableProcessors();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new ForkJoinPool(processors, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
        }
        return new ThreadPoolExecutor(processors, processors, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().build());
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

        public void join() {
            try {
                taskCount.decrementAndGet();
                doneLatch.await();
            } catch (final java.lang.Throwable $ex) {
                throw new RuntimeException($ex);
            }
        }

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public AtomicLong getTaskCount() {
            return this.taskCount;
        }

        @SuppressWarnings("all")
        public CountDownLatch getDoneLatch() {
            return this.doneLatch;
        }

        @SuppressWarnings("all")
        public LinkedBlockingQueue<T> getFutureResults() {
            return this.futureResults;
        }

        @SuppressWarnings("all")
        public void setTaskCount(final AtomicLong taskCount) {
            this.taskCount = taskCount;
        }

        @SuppressWarnings("all")
        public void setDoneLatch(final CountDownLatch doneLatch) {
            this.doneLatch = doneLatch;
        }

        @SuppressWarnings("all")
        public void setFutureResults(final LinkedBlockingQueue<T> futureResults) {
            this.futureResults = futureResults;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof MultiprocessingUtil.TaskBundle)) return false;
            final MultiprocessingUtil.TaskBundle<?> other = (MultiprocessingUtil.TaskBundle<?>) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$taskCount = this.getTaskCount();
            final Object other$taskCount = other.getTaskCount();
            if (this$taskCount == null ? other$taskCount != null : !this$taskCount.equals(other$taskCount)) return false;
            final Object this$doneLatch = this.getDoneLatch();
            final Object other$doneLatch = other.getDoneLatch();
            if (this$doneLatch == null ? other$doneLatch != null : !this$doneLatch.equals(other$doneLatch)) return false;
            final Object this$futureResults = this.getFutureResults();
            final Object other$futureResults = other.getFutureResults();
            if (this$futureResults == null ? other$futureResults != null : !this$futureResults.equals(other$futureResults)) return false;
            return true;
        }

        @SuppressWarnings("all")
        protected boolean canEqual(final Object other) {
            return other instanceof MultiprocessingUtil.TaskBundle;
        }

        @Override
        @SuppressWarnings("all")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $taskCount = this.getTaskCount();
            result = result * PRIME + ($taskCount == null ? 43 : $taskCount.hashCode());
            final Object $doneLatch = this.getDoneLatch();
            result = result * PRIME + ($doneLatch == null ? 43 : $doneLatch.hashCode());
            final Object $futureResults = this.getFutureResults();
            result = result * PRIME + ($futureResults == null ? 43 : $futureResults.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "MultiprocessingUtil.TaskBundle(taskCount=" + this.getTaskCount() + ", doneLatch=" + this.getDoneLatch() + ", futureResults=" + this.getFutureResults() + ")";
        }

        @SuppressWarnings("all")
        public TaskBundle(final AtomicLong taskCount, final CountDownLatch doneLatch, final LinkedBlockingQueue<T> futureResults) {
            this.taskCount = taskCount;
            this.doneLatch = doneLatch;
            this.futureResults = futureResults;
        }
        //</editor-fold>
    }
}
