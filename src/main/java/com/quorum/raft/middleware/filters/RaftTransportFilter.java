package com.quorum.raft.middleware.filters;

import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;

import java.net.SocketAddress;
import java.util.logging.Logger;

public class RaftTransportFilter extends ServerTransportFilter {

    private static final Logger logger = Logger.getLogger(RaftTransportFilter.class.getName());

    @Override
    public Attributes transportReady(Attributes transportAttrs) {
        SocketAddress remoteAddrs = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        logger.info("Client connected from IP: " + remoteAddrs);
        return transportAttrs;
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        SocketAddress remoteAddrs = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        logger.info("Client disconnected from IP: " + remoteAddrs);
    }
}
