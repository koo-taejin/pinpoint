/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.metric;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public abstract class CustomMetricValueList<T extends CustomMetricValue> {

    private final String metricName;
    private final AgentStatType agentStatType;

    private final List<T> customMetricValueList = new ArrayList<>();

    public CustomMetricValueList(String metricName, AgentStatType agentStatType) {
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.agentStatType = Objects.requireNonNull(agentStatType, "agentStatType");
    }

    public String getMetricName() {
        return metricName;
    }

    public boolean add(T customMetricValue) {
        Assert.requireNonNull(customMetricValue, "customMetricValue");
        return customMetricValueList.add(customMetricValue);
    }

//    public boolean remove(T customMetricValue) {
//        Assert.requireNonNull(customMetricValue, "customMetricValue");
//        return customMetricValueList.remove(customMetricValue);
//    }

    public int size() {
        return CollectionUtils.nullSafeSize(customMetricValueList);
    }

    public List<T> getList() {
        return Collections.unmodifiableList(customMetricValueList);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("agentStatType=").append(agentStatType);
        sb.append(", customMetricValueList=").append(customMetricValueList);
        sb.append('}');
        return sb.toString();
    }

}
