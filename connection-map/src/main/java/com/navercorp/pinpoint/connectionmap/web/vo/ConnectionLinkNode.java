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

package com.navercorp.pinpoint.connectionmap.web.vo;

import com.navercorp.pinpoint.connectionmap.web.view.ConnectionLinkNodeSerializer;
import com.navercorp.pinpoint.connectionmap.web.view.TransferDataStatSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = ConnectionLinkNodeSerializer.class)
public class ConnectionLinkNode {

    private final ConnectionAddress connectionAddress;
    private final ConnectionNode connectionNode;

    ConnectionLinkNode(ConnectionAddress connectionAddress) {
        this(connectionAddress, null);
    }

    ConnectionLinkNode(ConnectionAddress connectionAddress, ConnectionNode connectionNode) {
        this.connectionAddress = Objects.requireNonNull(connectionAddress, "connectionAddress");
        this.connectionNode = connectionNode;
    }

    public boolean isIncludedAgentInfo() {
        return connectionNode != null;
    }

    public ConnectionNode getConnectionNode() {
        return connectionNode;
    }

    public ConnectionAddress getConnectionAddress() {
        return connectionAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionLinkNode that = (ConnectionLinkNode) o;

        if (connectionNode != null ? !connectionNode.equals(that.connectionNode) : that.connectionNode != null) return false;
        return connectionAddress != null ? connectionAddress.equals(that.connectionAddress) : that.connectionAddress == null;
    }

    @Override
    public int hashCode() {
        int result = connectionNode != null ? connectionNode.hashCode() : 0;
        result = 31 * result + (connectionAddress != null ? connectionAddress.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionLinkNode{");
        sb.append("connectionNode=").append(connectionNode);
        sb.append(", connectionAddress=").append(connectionAddress);
        sb.append('}');
        return sb.toString();
    }

}
