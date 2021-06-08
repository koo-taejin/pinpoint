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

import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionType;
import com.navercorp.pinpoint.connectionmap.common.proto.Direction;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionLinkNode;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ConnectionLinkKey {

    private final ConnectionLinkNode srcNode;
    private final ConnectionLinkNode dstNode;

    private final ConnectionType connectionType;

    private final Direction direction;

    public ConnectionLinkKey(ConnectionLinkNode srcNode, ConnectionLinkNode dstNode, ConnectionType connectionType, Direction direction) {
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.connectionType = connectionType;
        this.direction = Objects.requireNonNull(direction, "direction");
    }

    public ConnectionLinkNode getSrcNode() {
        return srcNode;
    }

    public ConnectionLinkNode getDstNode() {
        return dstNode;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean checkSameLink(ConnectionLinkKey connectionLinkKey) {
        if (!this.srcNode.equals(connectionLinkKey.getSrcNode())) {
            return false;
        }

        if (!this.dstNode.equals(connectionLinkKey.getDstNode())) {
            return false;
        }

        if (this.connectionType != connectionLinkKey.getConnectionType()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionLinkKey that = (ConnectionLinkKey) o;

        if (srcNode != null ? !srcNode.equals(that.srcNode) : that.srcNode != null) return false;
        if (dstNode != null ? !dstNode.equals(that.dstNode) : that.dstNode != null) return false;
        if (connectionType != that.connectionType) return false;
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = srcNode != null ? srcNode.hashCode() : 0;
        result = 31 * result + (dstNode != null ? dstNode.hashCode() : 0);
        result = 31 * result + (connectionType != null ? connectionType.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionLinkKey{");
        sb.append("srcNode=").append(srcNode);
        sb.append(", dstNode=").append(dstNode);
        sb.append(", connectionType=").append(connectionType);
        sb.append(", direction=").append(direction);
        sb.append('}');
        return sb.toString();
    }
}
