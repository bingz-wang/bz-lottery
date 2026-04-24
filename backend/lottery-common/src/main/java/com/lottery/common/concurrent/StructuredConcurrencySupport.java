package com.lottery.common.concurrent;

import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Component
public class StructuredConcurrencySupport {

    public <T> T execute(Callable<T> task) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var future = executor.submit(task);
            try {
                return future.get();
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof Exception exception) {
                    throw exception;
                }
                throw new IllegalStateException("Structured task failed", cause);
            }
        }
    }
}
