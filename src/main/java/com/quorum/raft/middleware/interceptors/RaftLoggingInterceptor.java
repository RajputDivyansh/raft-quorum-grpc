package com.quorum.raft.middleware.interceptors;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;


public class RaftLoggingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
          Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        Long startTime = System.nanoTime();

        // Call actual service logic and get back the listener
        ServerCall.Listener<ReqT> originalListener = serverCallHandler.startCall(serverCall, metadata);
        return new ForwardingServerCallListener<ReqT>() {
            @Override
            protected ServerCall.Listener<ReqT> delegate() {
                return originalListener;
            }

            @Override
            public void onComplete() {
                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                System.out.println(
                      "Request to " + serverCall.getMethodDescriptor().getFullMethodName()
                            + " completed in " + durationMs + " ms");
                super.onComplete();
            }
        };

        // Wrap it to intercept stream events (like onComplete)
//        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(originalListener) {
//            @Override
//            public void onComplete() {
//                long durationMs = (System.nanoTime() - startTime) / 1_000_000;
//                System.out.println(
//                      "Request to " + serverCall.getMethodDescriptor().getFullMethodName()
//                            + " completed in " + durationMs + " ms");
//                super.onComplete();
//            }
//        };
    }
}
