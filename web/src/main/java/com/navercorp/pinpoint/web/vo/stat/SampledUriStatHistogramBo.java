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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Map;

/**
 * @author Taejin Koo
 */
public class SampledUriStatHistogramBo {

    public static final Long UNCOLLECTED_VALUE = -1L;
    public static final Double UNCOLLECTED_PERCENTAGE = -1D;

    public static final Point.UncollectedPointCreator<AgentStatPoint<Integer>> UNCOLLECTED_INT_VALUE_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Integer>>() {
        @Override
        public AgentStatPoint<Integer> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, -1);
        }
    };

    public static final Point.UncollectedPointCreator<AgentStatPoint<Long>> UNCOLLECTED_VALUE_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Long>>() {
        @Override
        public AgentStatPoint<Long> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_VALUE);
        }
    };
    public static final Point.UncollectedPointCreator<AgentStatPoint<Double>> UNCOLLECTED_PERCENTAGE_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Double>>() {
        @Override
        public AgentStatPoint<Double> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_PERCENTAGE);
        }
    };


    private final AgentStatPoint<Integer> totalCount;
    private final AgentStatPoint<Long> maxElapsed;
    private final AgentStatPoint<Double> avg;
    private final Map<UriStatHistogramBucket, Long> uriStatHistogramBucketIntegerMap;
    private final long totalElapsedTime;

    public SampledUriStatHistogramBo(AgentStatPoint<Integer> totalCount, AgentStatPoint<Long> maxElapsed, AgentStatPoint<Double> avg, Map<UriStatHistogramBucket, Long> uriStatHistogramBucketIntegerMap, long totalElapsedTime) {
        this.totalCount = totalCount;
        this.maxElapsed = maxElapsed;
        this.avg = avg;
        this.uriStatHistogramBucketIntegerMap = uriStatHistogramBucketIntegerMap;
        this.totalElapsedTime = totalElapsedTime;
    }

    public AgentStatPoint<Integer> getTotalCount() {
        return totalCount;
    }

    public AgentStatPoint<Long> getMaxElapsed() {
        return maxElapsed;
    }

    public AgentStatPoint<Double> getAvg() {
        return avg;
    }

    public long getTotalElapsedTime() {
        return totalElapsedTime;
    }

    public Map<UriStatHistogramBucket, Long> getUriStatHistogramBucketIntegerMap() {
        return uriStatHistogramBucketIntegerMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampledUriStatHistogramBo that = (SampledUriStatHistogramBo) o;

        if (totalCount != null ? !totalCount.equals(that.totalCount) : that.totalCount != null) return false;
        if (maxElapsed != null ? !maxElapsed.equals(that.maxElapsed) : that.maxElapsed != null) return false;
        if (avg != null ? !avg.equals(that.avg) : that.avg != null) return false;
        return uriStatHistogramBucketIntegerMap != null ? uriStatHistogramBucketIntegerMap.equals(that.uriStatHistogramBucketIntegerMap) : that.uriStatHistogramBucketIntegerMap == null;
    }

    @Override
    public int hashCode() {
        int result = totalCount != null ? totalCount.hashCode() : 0;
        result = 31 * result + (maxElapsed != null ? maxElapsed.hashCode() : 0);
        result = 31 * result + (avg != null ? avg.hashCode() : 0);
        result = 31 * result + (uriStatHistogramBucketIntegerMap != null ? uriStatHistogramBucketIntegerMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledUriStatHistogramBo{");
        sb.append("totalCount=").append(totalCount);
        sb.append(", maxElapsed=").append(maxElapsed);
        sb.append(", avg=").append(avg);
        sb.append(", uriStatHistogramBucketIntegerMap=").append(uriStatHistogramBucketIntegerMap);
        sb.append('}');
        return sb.toString();
    }
}
