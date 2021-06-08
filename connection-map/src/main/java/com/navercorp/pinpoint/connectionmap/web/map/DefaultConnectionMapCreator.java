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

package com.navercorp.pinpoint.connectionmap.web.map;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;
import com.navercorp.pinpoint.connectionmap.web.service.ConnectionDataMapService;
import com.navercorp.pinpoint.connectionmap.web.service.NetworkLocalConnectionService;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionLinkNode;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Service
public class DefaultConnectionMapCreator implements ConnectionMapCreator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConnectionDataMapService connectionDataMapService;
    private final NetworkLocalConnectionService networkLocalConnectionService;


    public DefaultConnectionMapCreator(ConnectionDataMapService connectionDataMapService, NetworkLocalConnectionService networkLocalConnectionService) {
        this.connectionDataMapService = Objects.requireNonNull(connectionDataMapService, "connectionDataMapService");
        this.networkLocalConnectionService = Objects.requireNonNull(networkLocalConnectionService, "networkLocalConnectionService");
    }

    @Override
    public ConnectionLinkDataMap create(List<ConnectionNode> connectionNodeList, ConnectionSelectContext connectionSelectContext) {
        if (CollectionUtils.isEmpty(connectionNodeList)) {
            return new ConnectionLinkDataMap();
        }

        ConnectionLinkDataMap result = new ConnectionLinkDataMap();
        for (ConnectionNode connectionNode : connectionNodeList) {
            if (!connectionSelectContext.isOutgoingDepthOverflow()) {
                if (!connectionSelectContext.checkVisitedOutgoing(connectionNode)) {
                    String agentId = connectionNode.getAgentId();
                    int pid = connectionNode.getPid();

                    Set<LocalIpVo> localIpVos = networkLocalConnectionService.selectLocalAddressConnectionInfo(agentId, connectionSelectContext.getRange());
                    for (LocalIpVo localIpVo : localIpVos) {
                        ConnectionLinkDataMap connectionLinkDataMap = connectionDataMapService.selectOutgoingConnectionDataMap(agentId, localIpVo.getSrcIp(), pid, connectionSelectContext.getRange());
                        result.merge(connectionLinkDataMap);

                        Set<ConnectionLinkKey> linkKeyList = connectionLinkDataMap.getLinkKeyList();
                        for (ConnectionLinkKey connectionLinkKey : linkKeyList) {
                            ConnectionLinkNode dstNode = connectionLinkKey.getDstNode();

                            ConnectionNode dstConnectionNode = dstNode.getConnectionNode();
                            if (dstConnectionNode != null) {
                                String agentId1 = dstConnectionNode.getAgentId();
                                int pid1 = dstConnectionNode.getPid();

                                ConnectionNode connectionNode1 = new ConnectionNode(agentId1, pid1);
                                connectionSelectContext.addNextConnectionNode(connectionNode1);
                            }
                            connectionSelectContext.visitOutgoing(connectionNode);
                        }
                    }
                }
            }

            if (!connectionSelectContext.isIncomingDepthOverflow()) {
                if (!connectionSelectContext.checkVisitedIncoming(connectionNode)) {
                    String agentId = connectionNode.getAgentId();
                    int pid = connectionNode.getPid();

                    Set<LocalIpVo> localIpVos = networkLocalConnectionService.selectLocalAddressConnectionInfo(agentId, connectionSelectContext.getRange());

                    for (LocalIpVo localIpVo : localIpVos) {
                        ConnectionLinkDataMap connectionLinkDataMap = connectionDataMapService.selectIncomingConnectionDataMap(agentId, localIpVo.getSrcIp(), pid, connectionSelectContext.getRange());
                        result.merge(connectionLinkDataMap);

                        Set<ConnectionLinkKey> linkKeyList = connectionLinkDataMap.getLinkKeyList();
                        for (ConnectionLinkKey connectionLinkKey : linkKeyList) {
                            ConnectionLinkNode srcNode = connectionLinkKey.getSrcNode();
                            String agentId1 = srcNode.getConnectionNode().getAgentId();
                            int pid1 = srcNode.getConnectionNode().getPid();

                            ConnectionNode connectionNode1 = new ConnectionNode(agentId1, pid1);
                            connectionSelectContext.addNextConnectionNode(connectionNode1);

                            connectionSelectContext.visitIncoming(connectionNode);
                        }

                    }
                }
            }
        }
        return result;
    }

}
