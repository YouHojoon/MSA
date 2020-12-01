package com.thoughtmechanix.licenses.hystrix;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.thoughtmechanix.licenses.util.UserContextHolder;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class ThreadLocalAwareStrategy extends HystrixConcurrencyStrategy {

    private final HystrixConcurrencyStrategy existConcurrencyStrategy;

    @Override
    public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
        return existConcurrencyStrategy != null ? existConcurrencyStrategy.getBlockingQueue(maxQueueSize) :
                super.getBlockingQueue(maxQueueSize);
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        return existConcurrencyStrategy != null ? existConcurrencyStrategy.wrapCallable(new DelegatingUserContextCallable<T>(callable, UserContextHolder.getContext()))
                : super.wrapCallable(new DelegatingUserContextCallable<T>(callable, UserContextHolder.getContext()));
    }
}
