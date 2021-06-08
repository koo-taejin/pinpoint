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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ConnectionLinkDataMap {

    private final Map<ConnectionLinkKey, ConnectionLinkValue> connectionLinkMap = new HashMap<>();

    public void addLinkData(ConnectionLink link) {
        Objects.requireNonNull(link, "link");

        ConnectionLinkKey key = link.getKey();
        ConnectionLinkValue value = link.getValue();

        ConnectionLinkValue connectionLinkValue = connectionLinkMap.get(key);
        if (connectionLinkValue != null) {
            connectionLinkValue.merge(value);
        } else {
            connectionLinkMap.put(key, value);
        }
    }

    public void merge(ConnectionLinkDataMap connectionLinkDataMap) {
        for (Map.Entry<ConnectionLinkKey, ConnectionLinkValue> connectionLinkKeyConnectionLinkValueEntry : connectionLinkDataMap.connectionLinkMap.entrySet()) {
            ConnectionLinkKey key = connectionLinkKeyConnectionLinkValueEntry.getKey();
            ConnectionLinkValue value = connectionLinkKeyConnectionLinkValueEntry.getValue();

            ConnectionLink connectionLink = new ConnectionLink(key, value);
            addLinkData(connectionLink);
        }
    }

    public Set<ConnectionLinkKey> getLinkKeyList() {
        return connectionLinkMap.keySet();
    }

    public ConnectionLink getLinkData(ConnectionLinkKey connectionLinkKey) {
        Objects.requireNonNull(connectionLinkKey, "connectionLinkKey");

        ConnectionLinkValue connectionLinkValue = this.connectionLinkMap.get(connectionLinkKey);
        return new ConnectionLink(connectionLinkKey, connectionLinkValue);
    }

    public int size() {
        return connectionLinkMap.size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionLinkDataMap{");
        sb.append("linkDataMap=").append(connectionLinkMap);
        sb.append('}');
        return sb.toString();
    }
}
