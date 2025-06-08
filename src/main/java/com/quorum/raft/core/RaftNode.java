package com.quorum.raft.core;

import com.quorum.raft.enums.Role;
import com.quorum.raft.rpc.RaftRpcClient;
import com.quorum.raft.rpc_proto.RaftProto;

import java.util.ArrayList;
import java.util.List;

public class RaftNode {

    // Persistent State (stored on disk ideally)
    private Integer currentTerm = 0;
    private Integer votedFor = null;
    private final List<LogEntry> log = new ArrayList<>();

    // Volatile State
    private Role role = Role.FOLLOWER;
    private int commitIndex = 0;
    private int lastApplied = 0;

    // For Leaders
    private List<Integer> nextIndex;
    private List<Integer> matchIndex;

    private final Integer nodeId; // unique ID of this node
    private final List<Integer> peers; // list of peer host:port

    private long lastHeartbeatTime = System.currentTimeMillis();

    private final RaftRpcClient rpcClient;

    public RaftNode(int nodeId, List<Integer> peers, RaftRpcClient rpcClient) {
        this.nodeId = nodeId;
        this.peers = peers;
        this.rpcClient = rpcClient;
    }

    public synchronized void becomeFollower(int term) {
        this.role = Role.FOLLOWER;
        this.currentTerm = term;
        this.votedFor = null;
        System.out.println(nodeId + " became FOLLOWER in term " + term);
    }

    public synchronized void becomeCandidate() {
        this.role = Role.CANDIDATE;
        this.currentTerm++;
        this.votedFor = nodeId;
        System.out.println(nodeId + " became CANDIDATE in term " + currentTerm);
    }

    public synchronized void becomeLeader() {
        this.role = Role.LEADER;
        System.out.println(nodeId + " became LEADER in term " + currentTerm);

        // Initialize leader state
        int lastLogIndex = log.size();
        nextIndex = new ArrayList<>();
        matchIndex = new ArrayList<>();
        for (int i = 0; i < peers.size(); i++) {
            nextIndex.add(lastLogIndex + 1);
            matchIndex.add(0);
        }
    }

    public synchronized void startElection() {
        System.out.println("Node " + nodeId + " is starting an election in term " + currentTerm);
        int[] votesGranted = {1};
        for (int peerId : peers) {
            new Thread(() -> {
                String target = "localhost:500" + peerId;

                RaftProto.VoteRequest request = RaftProto.VoteRequest.newBuilder()
                      .setTerm(currentTerm)
                      .setCandidateId(nodeId)
                      .setLastLogIndex(log.size() - 1)
                      .setLastLogTerm((!log.isEmpty()) ? log.getLast().getTerm() : 0)
                      .build();

                RaftProto.VoteResponse response = rpcClient.requestVote(target, request);

                if (response == null) return;

                synchronized (RaftNode.this) {
                    if (response.getTerm() > currentTerm) {
                        becomeFollower(response.getTerm());
                    } else if (response.getVoteGranted()) {
                        votesGranted[0]++;
                        if (votesGranted[0] > (peers.size() + 1) / 2 && role == Role.CANDIDATE) {
                            becomeLeader();
                        }
                    }
                }
            }).start();
        }

    }


    public synchronized int getCurrentTerm() {
        return currentTerm;
    }

    public synchronized Integer getVotedFor() {
        return votedFor;
    }

    public synchronized Role getRole() {
        return role;
    }

    public synchronized void setVotedFor(Integer votedFor) {
        this.votedFor = votedFor;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public List<Integer> getPeers() {
        return peers;
    }

    public synchronized long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public synchronized void updateLastHeartbeatTime() {
        lastHeartbeatTime = System.currentTimeMillis();
    }

    // Additional methods for log replication, vote handling, etc. will go here
}
