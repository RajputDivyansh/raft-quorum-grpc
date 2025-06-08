package com.quorum.raft.middleware.tracers;

public class LoggingStreamTracer extends BaseServerStreamTracer {

    private final String fullMethodName;

    public LoggingStreamTracer(String fullMethodName) {
        super();
        this.fullMethodName = fullMethodName;
    }

    @Override
    public void serverCallStarted(ServerCallInfo<?, ?> callInfo) {
        logger.info("[" + requestId + "] RPC started for method: " + fullMethodName);
    }
}
