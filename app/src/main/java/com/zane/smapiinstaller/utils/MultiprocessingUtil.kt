package com.zane.smapiinstaller.utils

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong


/**
 * @author Zane
 */
object MultiprocessingUtil {
    val EXECUTOR_SERVICE = executorService
    val executorService: ExecutorService
        get() {
            val processors = Runtime.getRuntime().availableProcessors()
            return ForkJoinPool(
                processors, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true
            )
        }

    fun <T> newTaskBundle(onResult: (T) -> Unit): TaskBundle<T> {
        val futureResults = LinkedBlockingQueue<T>()
        val taskCount = AtomicLong(1)
        val doneLatch = CountDownLatch(1)
        EXECUTOR_SERVICE.submit {
            while (true) {
                try {
                    val result = futureResults.poll(50, TimeUnit.MILLISECONDS)
                    if (result != null) {
                        onResult.invoke(result)
                    } else {
                        if (taskCount.get() == 0L) {
                            if (futureResults.isEmpty()) {
                                doneLatch.countDown()
                                return@submit
                            }
                        }
                    }
                } catch (ignored: InterruptedException) {
                    doneLatch.countDown()
                    return@submit
                }
            }
        }
        return TaskBundle(taskCount, doneLatch, futureResults)
    }

    class TaskBundle<T>    //</editor-fold>
        (//<editor-fold defaultstate="collapsed" desc="delombok">
        var taskCount: AtomicLong,
        var doneLatch: CountDownLatch,
        var futureResults: LinkedBlockingQueue<T>
    ) {
        fun submitTask(task: () -> T) {
            EXECUTOR_SERVICE.submit {
                taskCount.incrementAndGet()
                try {
                    futureResults.add(task.invoke())
                } finally {
                    taskCount.decrementAndGet()
                }
            }
        }

        fun join() {
            try {
                taskCount.decrementAndGet()
                doneLatch.await()
            } catch (`$ex`: Throwable) {
                throw RuntimeException(`$ex`)
            }
        }
    }
}