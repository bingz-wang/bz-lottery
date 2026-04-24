package com.lottery.common.concurrent;

public final class ExecutionContext {

    private static final InheritableThreadLocal<String> TRACE_ID = new InheritableThreadLocal<>();

    private ExecutionContext() {
    }

    public static void bindTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String currentTraceId() {
        return TRACE_ID.get();
    }

    public static void clear() {
        TRACE_ID.remove();
    }
}
