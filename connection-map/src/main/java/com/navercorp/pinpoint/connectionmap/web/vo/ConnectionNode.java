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

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ConnectionNode {

    private final String agentId;
    private final int pid;

    public ConnectionNode(String agentId, int pid) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");

        if (pid < 0) {
            this.pid = -1;
        } else {
            this.pid = pid;
        }
    }

    public String getAgentId() {
        return agentId;
    }

    public int getPid() {
        return pid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionNode that = (ConnectionNode) o;

        if (pid != that.pid) return false;
        return agentId != null ? agentId.equals(that.agentId) : that.agentId == null;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + pid;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionNode{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", pid=").append(pid);
        sb.append('}');
        return sb.toString();
    }
}
