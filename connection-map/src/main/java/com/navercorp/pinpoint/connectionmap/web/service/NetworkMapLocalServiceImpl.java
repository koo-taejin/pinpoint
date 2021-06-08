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

import com.navercorp.pinpoint.connectionmap.common.proto.Direction;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;
import com.navercorp.pinpoint.connectionmap.web.dao.ConnectionStatSelfDao;
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

/**
 * @author Taejin Koo
 */
@Service
public class NetworkMapLocalServiceImpl implements NetworkMapLocalService {

    private final AddressStatService addressStatService;
    private final PidStatService pidStatService;
    private final ConnectionStatSelfDao connectionStatSelfDao;
    private final NetworkConnectionService networkConnectionService;
    private final TimeWindowFactory timeWindowFactory;

    public NetworkMapLocalServiceImpl(AddressStatService addressStatService, PidStatService pidStatService, ConnectionStatSelfDao connectionStatSelfDao, NetworkConnectionService networkConnectionService, TimeWindowFactory timeWindowFactory) {
        this.addressStatService = Objects.requireNonNull(addressStatService, "addressStatService");
        this.pidStatService = Objects.requireNonNull(pidStatService, "pidStatService");
        this.connectionStatSelfDao = Objects.requireNonNull(connectionStatSelfDao, "connectionStatSelfDao");
        this.networkConnectionService = Objects.requireNonNull(networkConnectionService, "networkConnectionService");
        this.timeWindowFactory = Objects.requireNonNull(timeWindowFactory, "timeWindowFactory");
    }

    @Override
    public List<ConnectionLink> selectLocalMap(String agentId, int pid, ValueRange range) {
        return null;
    }

    @Override
    public Map<Integer, Map<ConnectionLinkKey, ConnectionLinkValue>> selectRootLocalMap(String agentId, ValueRange range) {
        TimeWindow timeWindow = timeWindowFactory.create(range);

        Set<LocalIpVo> localAddresses = addressStatService.selectLocalAddresses(agentId, range);
        Set<Integer> pidSet = pidStatService.selectPids(agentId, range);

        Map<Integer, Map<ConnectionLinkKey, ConnectionLinkValue>> map = new HashMap<>();

        for (LocalIpVo localAddress : localAddresses) {
            byte[] srcIp = localAddress.getSrcIp();

            List<ConnectionStatVo> outgoingConnectionStatVoList = networkConnectionService.selectOutgoingConnectionDataMap(agentId, srcIp, pidSet, range);

            for (ConnectionStatVo outgoingConnectionStatVo : outgoingConnectionStatVoList) {
                int pid = outgoingConnectionStatVo.getPid();

                map.putIfAbsent(pid, new HashMap<>());
                Map<ConnectionLinkKey, ConnectionLinkValue> connectionLinkKeyConnectionLinkValueMap = map.get(pid);
                if (connectionLinkKeyConnectionLinkValueMap == null) {
                    continue;
                }

                for (Map.Entry<ConnectionLinkKey, ConnectionLinkValue> entry : connectionLinkKeyConnectionLinkValueMap.entrySet()) {
                    ConnectionLinkKey connectionKey = entry.getKey();
                    ConnectionLinkValue connectionLinkValue = entry.getValue();

                    if (connectionLinkValue == null) {
                        connectionLinkKeyConnectionLinkValueMap.put(connectionKey, new ConnectionLinkValue(timeWindow));
                        connectionLinkValue = connectionLinkKeyConnectionLinkValueMap.get(connectionKey);
                    }

                    connectionLinkValue.addLinkData(outgoingConnectionStatVo);
                }

            }
        }

        for (LocalIpVo localAddress : localAddresses) {
            byte[] dstIp = localAddress.getSrcIp();

            List<ConnectionStatVo> connectionStatVoList = networkConnectionService.selectIncomingConnectionDataMap(agentId, dstIp, pidSet, range);

            for (ConnectionStatVo connectionStatVo : connectionStatVoList) {
                int pid = connectionStatVo.getPid();

                map.putIfAbsent(pid, new HashMap<>());
                Map<ConnectionLinkKey, ConnectionLinkValue> connectionLinkKeyConnectionLinkValueMap = map.get(pid);

                ConnectionLinkKey connectionKey = null;
                ConnectionLinkValue connectionLinkValue = connectionLinkKeyConnectionLinkValueMap.get(connectionKey);

                if (connectionLinkValue == null) {
                    connectionLinkKeyConnectionLinkValueMap.put(connectionKey, new ConnectionLinkValue(timeWindow));
                    connectionLinkValue = connectionLinkKeyConnectionLinkValueMap.get(connectionKey);
                }

                connectionLinkValue.addLinkData(connectionStatVo);
            }

        }

        for (Integer pid : pidSet) {
            List<ConnectionStatVo> connectionStatVoList = connectionStatSelfDao.getConnectionStatVos(agentId, pid, range);

            for (ConnectionStatVo connectionStatVo : connectionStatVoList) {
                int aPid = connectionStatVo.getPid();

                map.putIfAbsent(aPid, new HashMap<>());
                Map<ConnectionLinkKey, ConnectionLinkValue> connectionLinkKeyConnectionLinkValueMap = map.get(aPid);

//                ConnectionLinkKey connectionKey = createConnectionKey(connectionStatVo);
                ConnectionLinkKey connectionKey = null;
                ConnectionLinkValue connectionLinkValue = connectionLinkKeyConnectionLinkValueMap.get(connectionKey);

                if (connectionLinkValue == null) {
                    connectionLinkKeyConnectionLinkValueMap.put(connectionKey, new ConnectionLinkValue(timeWindow));
                    connectionLinkValue = connectionLinkKeyConnectionLinkValueMap.get(connectionKey);
                }

                connectionLinkValue.addLinkData(connectionStatVo);
            }
        }


        for (Integer pid : pidSet) {
            if (!map.containsKey(pid)) {
                map.put(pid, new HashMap<>());
            }
        }

        return map;
    }

    @Override
    public ConnectionLinkDataMap selectLocalMap(String agentId, ValueRange range) {
        ConnectionLinkDataMap result = new ConnectionLinkDataMap();

        Set<Integer> pidSet = pidStatService.selectPids(agentId, range);

        List<ConnectionStatVo> connectionStatVoList = new ArrayList<>();
        for (Integer pid : pidSet) {
            List<ConnectionStatVo> eachPidConnectionStatVoList = connectionStatSelfDao.getConnectionStatVos(agentId, pid, range);
            connectionStatVoList.addAll(eachPidConnectionStatVoList);
        }

        for (ConnectionStatVo connectionStatVo : connectionStatVoList) {
            TimeWindow timeWindow = timeWindowFactory.create(range);
            ConnectionLink connectionLink = creatLink(connectionStatVo, timeWindow);
            result.addLinkData(connectionLink);
        }

        return result;
    }

    private ConnectionLink creatLink(ConnectionStatVo connectionStatVo, TimeWindow timeWindow) {
        final ConnectionLinkNode srcConnectionNode = ConnectionLinkNodeFactory.createSrc(connectionStatVo, true);
        final ConnectionLinkNode dstConnectionNode = ConnectionLinkNodeFactory.createDst(connectionStatVo, true);

        ConnectionLinkKey connectionLinkKey = new ConnectionLinkKey(srcConnectionNode, dstConnectionNode,
                connectionStatVo.getConnectionType(), connectionStatVo.getDirection());
        ConnectionLinkValue connectionLinkValue = new ConnectionLinkValue(timeWindow);
        connectionLinkValue.addLinkData(connectionStatVo);

        return new ConnectionLink(connectionLinkKey, connectionLinkValue);
    }

    private Map<ConnectionAddress, List<ConnectionStatVo>> createAddressMap(List<ConnectionStatVo> connectionStatVos, Direction direction) {
        Map<ConnectionAddress, List<ConnectionStatVo>> result = new HashMap<>();

        for (ConnectionStatVo connectionStatVo : connectionStatVos) {
            final ConnectionAddress connectionAddress = ConnectionAddressFactory.createSrc(connectionStatVo);

            List<ConnectionStatVo> connectionStatVoList = result.get(connectionAddress);
            if (connectionStatVoList == null) {
                result.put(connectionAddress, new ArrayList<>());
                connectionStatVoList = result.get(connectionAddress);
            }

            connectionStatVoList.add(connectionStatVo);
        }

        return result;
    }

}
