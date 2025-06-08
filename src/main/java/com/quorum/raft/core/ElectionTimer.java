package com.quorum.raft.core;

import com.quorum.raft.enums.Role;

import java.util.Random;

public class ElectionTimer extends Thread {

    private final RaftNode raftNode;
    private final Random random = new Random();
    private volatile boolean running = true;

    public ElectionTimer(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Sleep for a randomized timeout between 150ms and 300ms
                int timeout = 1500 + random.nextInt(1500);
                Thread.sleep(timeout);

                if (raftNode.getRole() != Role.LEADER &&
                      System.currentTimeMillis() - raftNode.getLastHeartbeatTime() > timeout) {

                    raftNode.becomeCandidate();
                    raftNode.startElection();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
