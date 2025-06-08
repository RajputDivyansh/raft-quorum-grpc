package com.quorum.raft.server;

import com.quorum.raft.core.ElectionTimer;
import com.quorum.raft.core.RaftNode;
import com.quorum.raft.middleware.filters.RaftTransportFilter;
import com.quorum.raft.middleware.interceptors.RaftLoggingInterceptor;
import com.quorum.raft.middleware.tracers.RaftStreamTracerFactory;
import com.quorum.raft.rpc.RaftRpcClient;
import com.quorum.raft.rpc.RaftServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.internal.ServerImplBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RaftServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: RaftServer <nodeId> <port>");
            System.exit(1);
        }

        int nodeId = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);

        // Define all node IDs in the cluster (manually hardcoded for now)
        List<Integer> allNodeIds = Arrays.asList(1, 2, 3);

        // Exclude self from peers
        List<Integer> peers = allNodeIds.stream()
              .filter(id -> id != nodeId)
              .toList();

        // Initialize RaftNode and RaftServiceImpl
        RaftRpcClient rpcClient = new RaftRpcClient();
        RaftNode raftNode = new RaftNode(nodeId, peers, rpcClient);
        RaftServiceImpl service = new RaftServiceImpl(raftNode);

        // Start gRPC server
        Server server = ServerBuilder.forPort(port)
              .addService(service)
              .addTransportFilter(new RaftTransportFilter())
              .addStreamTracerFactory(new RaftStreamTracerFactory())
              .intercept(new RaftLoggingInterceptor())
              .build()
              .start();

        System.out.println("Raft node " + nodeId + " started on port " + port);

        // After starting gRPC server
        ElectionTimer timer = new ElectionTimer(raftNode);
        timer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down node " + nodeId);
            timer.shutdown();
        }));

        server.awaitTermination();
    }
}
