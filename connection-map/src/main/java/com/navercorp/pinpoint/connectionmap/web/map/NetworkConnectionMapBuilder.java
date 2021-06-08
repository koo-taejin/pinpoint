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

import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionLinkNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class NetworkConnectionMapBuilder {

    public ConnectionMap build(ConnectionLinkDataMap connectionLinkDataMap) {

        Set<ConnectionLinkNode> nodeList = new HashSet<>();
        List<ConnectionLink> connectionLinkList = new ArrayList<>();

        Set<ConnectionLinkKey> linkKeyList = connectionLinkDataMap.getLinkKeyList();

        for (ConnectionLinkKey connectionLinkKey : linkKeyList) {
            ConnectionLinkNode srcLinkNode = connectionLinkKey.getSrcNode();
            nodeList.add(srcLinkNode);

            ConnectionLinkNode dstLinkNode = connectionLinkKey.getDstNode();
            nodeList.add(dstLinkNode);


            ConnectionLinkValue value = connectionLinkDataMap.getLinkData(connectionLinkKey).getValue();
            ConnectionLink connectionLink = new ConnectionLink(connectionLinkKey, value);
            connectionLinkList.add(connectionLink);
        }

        return new DefaultConnectionMap(nodeList, connectionLinkList);

    }

}
