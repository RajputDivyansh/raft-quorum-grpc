package com.quorum.raft.middleware.tracers;

import io.grpc.Metadata;
import io.grpc.ServerStreamTracer;
import io.grpc.Status;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class RaftStreamTracerFactory extends ServerStreamTracer.Factory {

    private static final Logger logger = Logger.getLogger(RaftStreamTracerFactory.class.getName());

    @Override
    public ServerStreamTracer newServerStreamTracer(String fullMethodName, Metadata headers) {
        logger.info("Incoming RPC call to method: " + fullMethodName);

        return new LoggingStreamTracer(fullMethodName);

//        return new ServerStreamTracer() {
//            private final long startTime = System.nanoTime();
//
//            @Override
//            public void serverCallStarted(ServerCallInfo<?, ?> callInfo) {
//                logger.info("[" + requestId + "] - Server call started: " + callInfo.getFullMethodName());
//            }
//
//            @Override
//            public void streamClosed(Status status) {
//                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
//                if (!status.isOk()) {
//                    failedRequests.incrementAndGet();
//                    logger.warning("[" + requestId + "] - RPC failed with status: " + status + ", duration: " + durationMs + "ms");
//                } else {
//                    logger.info("[" + requestId + "] - RPC completed successfully in " + durationMs + "ms");
//                }
//            }
//        };
    }
}
