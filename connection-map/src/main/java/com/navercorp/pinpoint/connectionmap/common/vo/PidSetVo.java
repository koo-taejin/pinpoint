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

package com.navercorp.pinpoint.connectionmap.common.vo;

import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class PidSetVo {

    private final String agentId;
    private final long timestamp;
    private final Set<Integer> pidSet;

    public PidSetVo(String agentId, long timestamp, Set<Integer> pidSet) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.pidSet = Objects.requireNonNull(pidSet, "pidSet");
    }

    public String getAgentId() {
        return agentId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Set<Integer> getPidSet() {
        return pidSet;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PidVo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", pidSet=").append(pidSet);
        sb.append('}');
        return sb.toString();
    }
}
