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

package com.navercorp.pinpoint.connectionmap.collector.vo.converter;

import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionFamily;
import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionType;
import com.navercorp.pinpoint.connectionmap.common.proto.Direction;
import com.navercorp.pinpoint.connectionmap.common.proto.PConnectionStats;
import com.navercorp.pinpoint.connectionmap.common.proto.PLocalAddressesMessage;
import com.navercorp.pinpoint.connectionmap.common.proto.PLocalIp;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalAddressesVo;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;
import com.navercorp.pinpoint.grpc.Header;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class ConnectionStatConverter {

    public ConnectionStatConverter() {
    }

    public ConnectionStatVo convert(PConnectionStats pConnectionStats, long timestamp, Header header) {
        return newConnectionStatVo(pConnectionStats, timestamp, header);
    }

    private ConnectionStatVo newConnectionStatVo(PConnectionStats pConnectionStats, long timestamp, Header header) {
        ConnectionStatVo.Builder builder = new ConnectionStatVo.Builder();

        String agentId = header.getAgentId();
        builder.agentId(agentId);

        ConnectionType connectionType = pConnectionStats.getType();
        builder.connectionType(connectionType);

        ConnectionFamily connectionFamily = pConnectionStats.getFamily();
        builder.connectionFamily(connectionFamily);

        Direction direction = pConnectionStats.getDirection();
        builder.direction(direction);

        byte[] srcIp = pConnectionStats.getSrcIp().toByteArray();
        builder.srcIp(srcIp);
        int srcPort = pConnectionStats.getSrcPort();
        builder.srcPort(srcPort);

        byte[] dstIp = pConnectionStats.getDstIp().toByteArray();
        builder.dstIp(dstIp);
        int dstPort = pConnectionStats.getDstPort();
        builder.dstPort(dstPort);

        long monotonicSentBytes = pConnectionStats.getMonotonicSentBytes();
        builder.monotonicSentBytes(monotonicSentBytes);
        long sentBytes = pConnectionStats.getSentBytes();
        builder.sentBytes(sentBytes);

        long monotonicRecvBytes = pConnectionStats.getMonotonicRecvBytes();
        builder.monotonicRecvBytes(monotonicRecvBytes);
        long recvBytes = pConnectionStats.getRecvBytes();
        builder.recvBytes(recvBytes);

        int monotonicRetransmits = pConnectionStats.getMonotonicRetransmits();
        builder.monotonicRetransmits(monotonicRetransmits);
        int lastRetransmits = pConnectionStats.getLastRetransmits();
        builder.lastRetransmits(lastRetransmits);

        int pid = pConnectionStats.getPid();
        builder.pid(pid);
        int netNs = pConnectionStats.getNetNs();
        builder.netNs(netNs);

        builder.timestamp(timestamp);

        return builder.build();
    }

    public LocalAddressesVo convert(PLocalAddressesMessage message, Header header) {
        LocalAddressesVo.Builder builder = new LocalAddressesVo.Builder();

        String agentId = header.getAgentId();
        builder.agentId(agentId);

        List<PLocalIp> localIpListList = ((PLocalAddressesMessage) message).getLocalIpListList();
        for (PLocalIp pLocalIp : localIpListList) {
            byte[] localIp = pLocalIp.getLocalIp().toByteArray();
            LocalIpVo localIpVo = new LocalIpVo(localIp);
            builder.addLocalIpVo(localIpVo);
        }

        return builder.build();
    }

}
