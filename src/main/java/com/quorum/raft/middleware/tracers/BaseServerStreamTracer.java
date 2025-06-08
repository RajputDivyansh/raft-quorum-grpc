package com.quorum.raft.middleware.tracers;

import io.grpc.ServerStreamTracer;
import io.grpc.Status;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class BaseServerStreamTracer extends ServerStreamTracer {

    protected final Logger logger = Logger.getLogger(BaseServerStreamTracer.class.getName());

    protected static final AtomicLong REQUESTED_COUNTER = new AtomicLong();
    protected final Long requestId;
    protected final Long startTimeNs;

    public BaseServerStreamTracer() {
        this.requestId = REQUESTED_COUNTER.incrementAndGet();
        this.startTimeNs = System.nanoTime();
    }

    @Override
    public void streamClosed(Status status) {
        long durationMs = (System.nanoTime() - startTimeNs) / 1_000_000;
        if (status.isOk()) {
            logger.info("[" + requestId + "] RPC completed in " + durationMs + " ms");
        } else {
            logger.warning("[" + requestId + "] RPC failed: " + status + ", duration: " + durationMs + " ms");
        }
    }
}
