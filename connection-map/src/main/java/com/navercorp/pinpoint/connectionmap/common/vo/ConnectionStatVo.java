/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.connectionmap.common.vo;

import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionFamily;
import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionType;
import com.navercorp.pinpoint.connectionmap.common.proto.Direction;
import com.navercorp.pinpoint.connectionmap.common.util.InetFamily;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ConnectionStatVo {

    private final String agentId;

    private final ConnectionType connectionType;
    private final InetFamily inetFamily;
    private final Direction direction;

    private final byte[] srcIp;
    private final int srcPort;
    private final byte[] dstIp;
    private final int dstPort;

    private final int pid;

    private final long monotonicSentBytes;
    private final long sentBytes;

    private final long monotonicRecvBytes;
    private final long recvBytes;

    private final int monotonicRetransmits;
    private final int lastRetransmits;

    private final long lastUpdateEpoch;
    private final int netNs;

    private final long timestamp;

    public String getAgentId() {
        return agentId;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public InetFamily getInetFamily() {
        return inetFamily;
    }

    public Direction getDirection() {
        return direction;
    }

    public byte[] getSrcIp() {
        return srcIp;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public byte[] getDstIp() {
        return dstIp;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getPid() {
        return pid;
    }

    public long getMonotonicSentBytes() {
        return monotonicSentBytes;
    }

    public long getSentBytes() {
        return sentBytes;
    }

    public long getMonotonicRecvBytes() {
        return monotonicRecvBytes;
    }

    public long getRecvBytes() {
        return recvBytes;
    }

    public int getMonotonicRetransmits() {
        return monotonicRetransmits;
    }

    public int getLastRetransmits() {
        return lastRetransmits;
    }

    public long getLastUpdateEpoch() {
        return lastUpdateEpoch;
    }

    public int getNetNs() {
        return netNs;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private ConnectionStatVo(Builder builder) {

        this.agentId = builder.agentId;

        this.connectionType = builder.connectionType;
        this.inetFamily = builder.inetFamily;
        this.direction = builder.direction;

        this.srcIp = builder.srcIp;
        this.srcPort = builder.srcPort;
        this.dstIp = builder.dstIp;
        this.dstPort = builder.dstPort;

        this.pid = builder.pid;

        this.monotonicSentBytes = builder.monotonicSentBytes;
        this.sentBytes = builder.sentBytes;

        this.monotonicRecvBytes = builder.monotonicRecvBytes;
        this.recvBytes = builder.recvBytes;

        this.monotonicRetransmits = builder.monotonicRetransmits;
        this.lastRetransmits = builder.lastRetransmits;

        this.lastUpdateEpoch = builder.lastUpdateEpoch;
        this.netNs = builder.netNs;

        this.timestamp = builder.timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionStatVo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", connectionType=").append(connectionType);
        sb.append(", inetFamily=").append(inetFamily);
        sb.append(", direction=").append(direction);
        sb.append(", srcIp=").append(Arrays.toString(srcIp));
        sb.append(", srcPort=").append(srcPort);
        sb.append(", dstIp=").append(Arrays.toString(dstIp));
        sb.append(", dstPort=").append(dstPort);
        sb.append(", pid=").append(pid);
        sb.append(", monotonicSentBytes=").append(monotonicSentBytes);
        sb.append(", sentBytes=").append(sentBytes);
        sb.append(", monotonicRecvBytes=").append(monotonicRecvBytes);
        sb.append(", recvBytes=").append(recvBytes);
        sb.append(", monotonicRetransmits=").append(monotonicRetransmits);
        sb.append(", lastRetransmits=").append(lastRetransmits);
        sb.append(", lastUpdateEpoch=").append(lastUpdateEpoch);
        sb.append(", netNs=").append(netNs);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {

        private String agentId;

        private ConnectionType connectionType;
        private InetFamily inetFamily;
        private Direction direction;

        private byte[] srcIp;
        private int srcPort;
        private byte[] dstIp;
        private int dstPort;

        private int pid;

        long monotonicSentBytes;
        long sentBytes;

        long monotonicRecvBytes;
        long recvBytes;

        int monotonicRetransmits;
        int lastRetransmits;

        long lastUpdateEpoch;
        int netNs;

        long timestamp;

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder connectionType(ConnectionType connectionType) {
            this.connectionType = connectionType;
            return this;
        }

        public Builder connectionFamily(ConnectionFamily connectionFamily) {
            this.inetFamily = InetFamily.getFamily(connectionFamily);
            return this;
        }

        public Builder inetFamily(InetFamily inetFamily) {
            this.inetFamily = Objects.requireNonNull(inetFamily, "inetFamily");
            return this;
        }

        public Builder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder srcIp(byte[] srcIp) {
            this.srcIp = srcIp;
            return this;
        }

        public Builder srcPort(int srcPort) {
            this.srcPort = srcPort;
            return this;
        }

        public Builder dstIp(byte[] dstIp) {
            this.dstIp = dstIp;
            return this;
        }

        public Builder dstPort(int dstPort) {
            this.dstPort = dstPort;
            return this;
        }

        public Builder pid(int pid) {
            this.pid = pid;
            return this;
        }

        public Builder monotonicSentBytes(long monotonicSentBytes) {
            this.monotonicSentBytes = monotonicSentBytes;
            return this;
        }

        public Builder sentBytes(long sentBytes) {
            this.sentBytes = sentBytes;
            return this;
        }

        public Builder monotonicRecvBytes(long monotonicRecvBytes) {
            this.monotonicRecvBytes = monotonicRecvBytes;
            return this;
        }

        public Builder recvBytes(long recvBytes) {
            this.recvBytes = recvBytes;
            return this;
        }

        public Builder monotonicRetransmits(int monotonicRetransmits) {
            this.monotonicRetransmits = monotonicRetransmits;
            return this;
        }

        public Builder lastRetransmits(int lastRetransmits) {
            this.lastRetransmits = lastRetransmits;
            return this;
        }

        public Builder lastUpdateEpoch(long lastUpdateEpoch) {
            this.lastUpdateEpoch = lastUpdateEpoch;
            return this;
        }

        public Builder netNs(int netNs) {
            this.netNs = netNs;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ConnectionStatVo build() {
            return new ConnectionStatVo(this);
        }

    }

}
