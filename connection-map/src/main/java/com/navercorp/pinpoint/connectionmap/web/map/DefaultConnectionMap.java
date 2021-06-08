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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DefaultConnectionMap implements ConnectionMap {

    private final Set<ConnectionLinkNode> nodeList;
    private final List<ConnectionLink> connectionLinks;

    public DefaultConnectionMap(Set<ConnectionLinkNode> nodeList, List<ConnectionLink> connectionLinks) {
        this.nodeList = Objects.requireNonNull(nodeList, "nodeList");
        this.connectionLinks = Objects.requireNonNull(connectionLinks, "connectionLinks");
    }

    @JsonProperty("nodeDataArray")
    @Override
    public Set<ConnectionLinkNode> getNodeList() {
        return nodeList;
    }

    @JsonProperty("linkDataArray")
    @Override
    public List<ConnectionLink> getConnectionLinks() {
        return connectionLinks;
    }

}
