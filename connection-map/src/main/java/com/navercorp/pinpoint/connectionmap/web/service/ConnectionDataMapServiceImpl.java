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

package com.navercorp.pinpoint.connectionmap.web.service;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.connectionmap.common.proto.Direction;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.web.dao.ConnectionStatCalleeDao;
import com.navercorp.pinpoint.connectionmap.web.dao.ConnectionStatCallerDao;
import com.navercorp.pinpoint.connectionmap.web.map.ConnectionLink;
import com.navercorp.pinpoint.connectionmap.web.map.ConnectionLinkDataMap;
import com.navercorp.pinpoint.connectionmap.web.map.ConnectionLinkKey;
import com.navercorp.pinpoint.connectionmap.web.map.ConnectionLinkValue;
import com.navercorp.pinpoint.connectionmap.web.util.TimeWindow;
import com.navercorp.pinpoint.connectionmap.web.util.TimeWindowFactory;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionAddress;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionAddressFactory;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionLinkNode;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionLinkNodeFactory;

import org.springframework.stereotype.Service;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
@Service
public class ConnectionDataMapServiceImpl implements ConnectionDataMapService {

    private final ConnectionStatCallerDao connectionStatCallerDao;
    private final ConnectionStatCalleeDao connectionStatCalleeDao;
    private final TimeWindowFactory timeWindowFactory;

    public ConnectionDataMapServiceImpl(ConnectionStatCallerDao connectionStatCallerDao, ConnectionStatCalleeDao connectionStatCalleeDao, TimeWindowFactory timeWindowFactory) {
        this.connectionStatCallerDao = connectionStatCallerDao;
        this.connectionStatCalleeDao = connectionStatCalleeDao;
        this.timeWindowFactory = Objects.requireNonNull(timeWindowFactory, "timeWindowFactory");
    }

    @Override
    public ConnectionLinkDataMap selectIncomingConnectionDataMap(String agentId, byte[] dstAddressIp, int pid, ValueRange range) {
        Objects.requireNonNull(agentId, "agentId");

        TimeWindow timeWindow = timeWindowFactory.create(range);

        List<ConnectionStatVo> incomingConnectionStatVoList = connectionStatCallerDao.getConnectionStatVos(dstAddressIp, range);
        List<ConnectionStatVo> filteredConnectionStatVoList = filter(incomingConnectionStatVoList, agentId, pid);

        // src - port map의 키로 가지고 있음
        Map<ConnectionAddress, List<ConnectionStatVo>> addressMap = createAddressMap(filteredConnectionStatVoList, Direction.INCOMING);
        Set<byte[]> srcIpAddressSet = filteredConnectionStatVoList.stream().map(v -> v.getSrcIp()).collect(Collectors.toSet());

        ConnectionLinkDataMap result = new ConnectionLinkDataMap();
        for (byte[] srcIpAddress : srcIpAddressSet) {
            // 타겟의 Outgoing Connection
            List<ConnectionStatVo> targetOutgoingConnectionList = connectionStatCalleeDao.getConnectionStatVos(srcIpAddress, range);

            for (ConnectionStatVo targetOutgoingConnection : targetOutgoingConnectionList) {
                final ConnectionAddress srcConnectionAddress = ConnectionAddressFactory.createSrc(targetOutgoingConnection);

                List<ConnectionStatVo> candidateConnectionList = addressMap.remove(srcConnectionAddress);
                List<ConnectionStatVo> foundBothConnectionList = candidateConnectionList.stream().filter(e -> e.getDstIp() == targetOutgoingConnection.getDstIp() && e.getDstPort() == targetOutgoingConnection.getDstPort()).collect(Collectors.toList());
                candidateConnectionList.removeAll(foundBothConnectionList);

                // find both connections that have agent
                ConnectionLink connectionLink = bindIncomingLink(targetOutgoingConnection, foundBothConnectionList, timeWindow);
                if (connectionLink != null) {
                    result.addLinkData(connectionLink);
                }

                // create only one connection has agent
                for (ConnectionStatVo candidate : candidateConnectionList) {
                    ConnectionLinkNode srcConnectionNode = ConnectionLinkNodeFactory.create(srcConnectionAddress, candidate, true);
                    ConnectionLinkNode dstConnectionNode = ConnectionLinkNodeFactory.createDst(candidate, false);

                    ConnectionLink link = createConnectionLink(srcConnectionNode, dstConnectionNode, candidate, timeWindow);
                    result.addLinkData(link);
                }
            }
        }

        for (Map.Entry<ConnectionAddress, List<ConnectionStatVo>> connectionAddressListEntry : addressMap.entrySet()) {
            ConnectionAddress srcConnectionAddress = connectionAddressListEntry.getKey();
            List<ConnectionStatVo> value = connectionAddressListEntry.getValue();

            for (ConnectionStatVo statVo : value) {
                ConnectionLinkNode srcConnectionNode = ConnectionLinkNodeFactory.create(srcConnectionAddress);
                ConnectionLinkNode dstConnectionNode = ConnectionLinkNodeFactory.createDst(statVo, true);

                ConnectionLink link = createConnectionLink(srcConnectionNode, dstConnectionNode, statVo, timeWindow);
                result.addLinkData(link);
            }
        }
        return result;
    }

    private ConnectionLink createConnectionLink(ConnectionLinkNode src, ConnectionLinkNode dst, ConnectionStatVo connectionStatVo, TimeWindow timeWindow) {
        ConnectionLinkKey key = new ConnectionLinkKey(src, dst, connectionStatVo.getConnectionType(), connectionStatVo.getDirection());
        ConnectionLinkValue value = new ConnectionLinkValue(timeWindow);
        value.addLinkData(connectionStatVo);

        return new ConnectionLink(key, value);
    }

    private List<ConnectionStatVo> filter(List<ConnectionStatVo> connectionStatVos, String agentId, int pid) {
        List<ConnectionStatVo> result = new ArrayList<>();

        for (ConnectionStatVo connectionStatVo : connectionStatVos) {
            if (agentId.equals(connectionStatVo.getAgentId())) {
                int pid1 = connectionStatVo.getPid();
                if (pid <= 0) {
                    result.add(connectionStatVo);
                } else if (pid1 == pid) {
                    result.add(connectionStatVo);
                }
            }
        }
        return result;
    }

    // find both connections that have agent
    private ConnectionLink bindOutgoingLink(ConnectionStatVo connectionStatVo, List<ConnectionStatVo> candidateConnectionList, TimeWindow timeWindow) {
        if (CollectionUtils.isEmpty(candidateConnectionList)) {
            return null;
        }

        final ConnectionAddress srcConnectionAddress = ConnectionAddressFactory.createSrc(connectionStatVo);
        final ConnectionAddress dstConnectionAddress = ConnectionAddressFactory.createDst(connectionStatVo);

        ConnectionLink connectionLink = null;
        for (ConnectionStatVo bindingAvailableConnection : candidateConnectionList) {
            ConnectionLinkNode srcConnectionNode = ConnectionLinkNodeFactory.create(srcConnectionAddress, connectionStatVo, true);
            ConnectionLinkNode dstConnectionNode = ConnectionLinkNodeFactory.create(dstConnectionAddress, bindingAvailableConnection, true);

            if (connectionLink == null) {
                ConnectionLinkKey connectionLinkKey = new ConnectionLinkKey(srcConnectionNode, dstConnectionNode, bindingAvailableConnection.getConnectionType(), bindingAvailableConnection.getDirection());
                ConnectionLinkValue connectionLinkValue = new ConnectionLinkValue(timeWindow);
                connectionLink = new ConnectionLink(connectionLinkKey, connectionLinkValue);
            }
            connectionLink.getValue().addLinkData(bindingAvailableConnection);
        }
        return connectionLink;
    }

    private ConnectionLink bindIncomingLink(ConnectionStatVo connectionStatVo, List<ConnectionStatVo> bindingAvailableConnectionList, TimeWindow timeWindow) {
        if (CollectionUtils.isEmpty(bindingAvailableConnectionList)) {
            return null;
        }

        final ConnectionAddress srcConnectionAddress = ConnectionAddressFactory.createSrc(connectionStatVo);
        final ConnectionAddress dstConnectionAddress = ConnectionAddressFactory.createDst(connectionStatVo);

        ConnectionLink connectionLink = null;
        for (ConnectionStatVo bindingAvailableConnection : bindingAvailableConnectionList) {
            ConnectionLinkNode srcConnectionNode = ConnectionLinkNodeFactory.create(srcConnectionAddress, bindingAvailableConnection, true);
            ConnectionLinkNode dstConnectionNode = ConnectionLinkNodeFactory.create(dstConnectionAddress, connectionStatVo, true);

            if (connectionLink == null) {
                ConnectionLinkKey connectionLinkKey = new ConnectionLinkKey(srcConnectionNode, dstConnectionNode, bindingAvailableConnection.getConnectionType(), bindingAvailableConnection.getDirection());
                ConnectionLinkValue connectionLinkValue = new ConnectionLinkValue(timeWindow);
                connectionLink = new ConnectionLink(connectionLinkKey, connectionLinkValue);
            }
            connectionLink.getValue().addLinkData(bindingAvailableConnection);
        }
        return connectionLink;
    }


    private Map<ConnectionAddress, List<ConnectionStatVo>> createAddressMap(List<ConnectionStatVo> connectionStatVos, Direction direction) {
        Map<ConnectionAddress, List<ConnectionStatVo>> result = new HashMap<>();

        for (ConnectionStatVo connectionStatVo : connectionStatVos) {
            ConnectionAddress connectionAddress = null;
            if (direction == Direction.INCOMING) {
                connectionAddress = ConnectionAddressFactory.createSrc(connectionStatVo);
            } else if (direction == Direction.OUTGOING) {
                connectionAddress = ConnectionAddressFactory.createDst(connectionStatVo);
            } else {
                throw new IllegalArgumentException("Invalid direction. direction:" + direction);
            }

            List<ConnectionStatVo> connectionStatVoList = result.get(connectionAddress);
            if (connectionStatVoList == null) {
                result.put(connectionAddress, new ArrayList<>());
                connectionStatVoList = result.get(connectionAddress);
            }

            connectionStatVoList.add(connectionStatVo);
        }

        return result;
    }

    @Override
    public ConnectionLinkDataMap selectOutgoingConnectionDataMap(String agentId, byte[] srcAddressIp, int pid, ValueRange range) {
        Objects.requireNonNull(agentId, "agentId");

        TimeWindow timeWindow = timeWindowFactory.create(range);

        List<ConnectionStatVo> outgoingConnectionStatVoList = connectionStatCalleeDao.getConnectionStatVos(srcAddressIp, range);
        List<ConnectionStatVo> filteredConnectionStatVoList = filter(outgoingConnectionStatVoList, agentId, pid);

        // src - port map의 키로 가지고 있음
        Map<ConnectionAddress, List<ConnectionStatVo>> addressMap = createAddressMap(filteredConnectionStatVoList, Direction.OUTGOING);
        final Set<byte[]> dstIpAddressSet = filteredConnectionStatVoList.stream().map(v -> v.getDstIp()).collect(Collectors.toSet());

        ConnectionLinkDataMap result = new ConnectionLinkDataMap();
        for (byte[] dstIpAddress : dstIpAddressSet) {
            // 타겟의 Incoming Connection
            List<ConnectionStatVo> targetIncomingConnectionList = connectionStatCallerDao.getConnectionStatVos(dstIpAddress, range);

            for (ConnectionStatVo targetIncomingConnection : targetIncomingConnectionList) {
                final ConnectionAddress dstConnectionAddress = ConnectionAddressFactory.createDst(targetIncomingConnection);

                List<ConnectionStatVo> candidateConnectionList = addressMap.remove(dstConnectionAddress);
                List<ConnectionStatVo> foundBothConnectionList = candidateConnectionList.stream().filter(e -> e.getSrcIp() == targetIncomingConnection.getSrcIp() && e.getSrcPort() == targetIncomingConnection.getSrcPort()).collect(Collectors.toList());
                candidateConnectionList.removeAll(foundBothConnectionList);

                ConnectionLink connectionLink = bindOutgoingLink(targetIncomingConnection, candidateConnectionList, timeWindow);
                if (connectionLink != null) {
                    result.addLinkData(connectionLink);
                }

                // create only one connection has agent
                for (ConnectionStatVo candidate : candidateConnectionList) {
                    ConnectionLinkNode srcConnectionNode = ConnectionLinkNodeFactory.createSrc(candidate, false);
                    ConnectionLinkNode dstConnectionNode = ConnectionLinkNodeFactory.create(dstConnectionAddress, candidate, true);

                    ConnectionLink link = createConnectionLink(srcConnectionNode, dstConnectionNode, candidate, timeWindow);
                    result.addLinkData(link);
                }
            }
        }

        for (Map.Entry<ConnectionAddress, List<ConnectionStatVo>> connectionAddressListEntry : addressMap.entrySet()) {

            ConnectionAddress dstConnectionAddress = connectionAddressListEntry.getKey();
            List<ConnectionStatVo> value = connectionAddressListEntry.getValue();

            for (ConnectionStatVo statVo : value) {
                ConnectionLinkNode dstConnectionNode = ConnectionLinkNodeFactory.create(dstConnectionAddress);
                ConnectionLinkNode srcConnectionNode = ConnectionLinkNodeFactory.createSrc(statVo, true);

                ConnectionLink link = createConnectionLink(srcConnectionNode, dstConnectionNode, statVo, timeWindow);
                result.addLinkData(link);
            }
        }

        return result;
    }

    @Override
    public List<ConnectionStatVo> selectIncomingConnectionDataMap(byte[] dstAddressIp, int dstPort, ValueRange range) {
        List<ConnectionStatVo> incomingConnectionList = connectionStatCallerDao.getConnectionStatVos(dstAddressIp, range);
        List<ConnectionStatVo> result = incomingConnectionList.stream().filter(c -> c.getDstPort() == dstPort).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<ConnectionStatVo> selectOutgoingConnectionDataMap(byte[] srcAddressIp, int srcPort, ValueRange range) {
        List<ConnectionStatVo> outgoingConnectionList = connectionStatCalleeDao.getConnectionStatVos(srcAddressIp, range);
        List<ConnectionStatVo> result = outgoingConnectionList.stream().filter(c -> c.getSrcPort() == srcPort).collect(Collectors.toList());
        return result;
    }

}