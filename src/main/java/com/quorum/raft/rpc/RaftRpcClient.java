package com.quorum.raft.rpc;

import com.quorum.raft.rpc_proto.RaftProto.*;
import com.quorum.raft.rpc_proto.RaftServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.openjdk.jol.info.ClassLayout;

public class RaftRpcClient {

    public VoteResponse requestVote(String targetHostPort, VoteRequest request) {
        try {
            ManagedChannel channel =
                  ManagedChannelBuilder.forTarget(targetHostPort).usePlaintext().build();

            RaftServiceGrpc.RaftServiceBlockingStub stub = RaftServiceGrpc.newBlockingStub(channel);
            VoteResponse response = stub.requestVote(request);

            channel.shutdownNow();
            return response;
        } catch (Exception e) {
            System.err.println(
                  "Failed to request vote from " + targetHostPort + ": " + e.getMessage());
            return null;
        }
    }

    // You can add appendEntries() here later
//    public static void main(String[] args) {
//        Object obj = new Object();
//        System.out.println("Identity Hash Code: " + System.identityHashCode(obj));
//        System.out.println(ClassLayout.parseInstance(obj).toPrintable());
//    }
}
