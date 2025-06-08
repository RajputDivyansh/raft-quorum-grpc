package com.quorum.raft.rpc;

import com.quorum.raft.core.RaftNode;
import com.quorum.raft.rpc_proto.RaftProto.*;
import com.quorum.raft.rpc_proto.RaftServiceGrpc.RaftServiceImplBase;
import io.grpc.stub.StreamObserver;

public class RaftServiceImpl extends RaftServiceImplBase {

    private final RaftNode raftNode;

    public RaftServiceImpl(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    @Override
    public void requestVote(VoteRequest request, StreamObserver<VoteResponse> responseObserver) {
        synchronized (raftNode) {
            boolean voteGranted = false;

            if (request.getTerm() > raftNode.getCurrentTerm()) {
                raftNode.becomeFollower(request.getTerm());
                raftNode.setVotedFor(null);
            }

            if (request.getTerm() == raftNode.getCurrentTerm() &&
                  (raftNode.getVotedFor() == null || raftNode.getVotedFor().equals(request.getCandidateId()))) {

                raftNode.setVotedFor(request.getCandidateId());
                voteGranted = true;
            }

            VoteResponse response = VoteResponse.newBuilder()
                  .setTerm(raftNode.getCurrentTerm())
                  .setVoteGranted(voteGranted)
                  .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void appendEntries(AppendRequest request, StreamObserver<AppendResponse> responseObserver) {
        synchronized (raftNode) {
            boolean success = false;

            if (request.getTerm() >= raftNode.getCurrentTerm()) {
                raftNode.becomeFollower(request.getTerm());
                success = true;
                // TODO: Add actual log matching logic here
            }

            raftNode.updateLastHeartbeatTime();

            AppendResponse response = AppendResponse.newBuilder()
                  .setTerm(raftNode.getCurrentTerm())
                  .setSuccess(success)
                  .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}

