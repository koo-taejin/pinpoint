/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.mapper.grpc.metric;

import com.navercorp.pinpoint.common.server.bo.metric.IntCounterMetricValue;
import com.navercorp.pinpoint.grpc.trace.PIntValue;

/**
 * @author Taejin Koo
 */
public class GrpcIntCountMetricMapper implements GrpcCustomMetricMapper<PIntValue, IntCounterMetricValue> {

    @Override
    public IntCounterMetricValue map(PIntValue value, IntCounterMetricValue prevValue) {
        if (value == null) {
            return null;
        }

        IntCounterMetricValue intCounterMetricValue = new IntCounterMetricValue();

        if (value.getIsNotSet()) {
            intCounterMetricValue.setValue(null);
        } else {
            if (prevValue == null || prevValue.getValue() == null) {
                intCounterMetricValue.setValue(value.getValue());
            } else {
                intCounterMetricValue.setValue(value.getValue() + prevValue.getValue());
            }
        }

        return intCounterMetricValue;
    }

}
