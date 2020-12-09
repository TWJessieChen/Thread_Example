package com.b09302083.myfuture;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyFuture<T> implements Future {

    private final CountDownLatch countDownLatch;

    private final Object lock = new Object();

    @GuardedBy("lock")
    private T t;
    @GuardedBy("lock")
    private Throwable throwable;
    @GuardedBy("lock")
    private boolean cancelled = false;

    public MyFuture() {
        countDownLatch = new CountDownLatch(1);
    }


    /**
     * @param mayInterruptIfRunning this value has no effect in the
     *                              default implementation because
     *                              interrupts are not used to
     *                              control cancellation.
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        synchronized (lock) {
            if (isDone()) {
                return false;
            } else {
                cancelled = true;
                countDownLatch.countDown();
                return true;
            }
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        if (throwable != null) {
            throw new ExecutionException(throwable);
        }
        if (isCancelled()) {
            throw new CancellationException();
        }
        return t;
    }

    @Override
    public T get(final long timeout, @NonNull final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final boolean inTime = countDownLatch.await(timeout, unit);
        if(!inTime) {
            throw new TimeoutException();
        }
        if (throwable != null) {
            throw new ExecutionException(throwable);
        }
        if (isCancelled()) {
            throw new CancellationException();
        }
        return t;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @GuardedBy("lock")
    @Override
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }

    public void done(final T t) {
        synchronized (lock) {
            if (!isDone()) {
                this.t = t;
                countDownLatch.countDown();
            }
        }
    }

    public void fail(final Throwable throwable) {
        synchronized (lock) {
            if (!isDone()) {
                this.throwable = throwable;
                countDownLatch.countDown();
            }
        }
    }

    public void done() {
        done(null);
    }
}