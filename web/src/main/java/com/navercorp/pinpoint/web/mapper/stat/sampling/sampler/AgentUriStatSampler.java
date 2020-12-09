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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.SampledUriStatHistogramBo;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
@Component
public class AgentUriStatSampler implements AgentStatSampler<EachUriStatBo, SampledEachUriStatBo> {

    private static Map<UriStatHistogramBucket, Long> EMPTY_URI_STAT_HISTOGRAM_MAP;
    static {
        Map<UriStatHistogramBucket, Long> map = new HashMap<>();
        for (UriStatHistogramBucket value : UriStatHistogramBucket.values()) {
            map.put(value, 0L);
        }
        EMPTY_URI_STAT_HISTOGRAM_MAP = Collections.unmodifiableMap(map);
    }

    private static final DownSampler<Integer> INTEGER_DOWN_SAMPLER = DownSamplers.getIntegerDownSampler(-1);
    private static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(SampledFileDescriptor.UNCOLLECTED_VALUE);
    private static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE, 0);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public SampledEachUriStatBo sampleDataPoints(int index, long timestamp, List<EachUriStatBo> eachUriStatBoList, EachUriStatBo previousDataPoint) {
        if (CollectionUtils.isEmpty(eachUriStatBoList)) {
            return null;
        }


        SampledEachUriStatBo sampledEachUriStatBo = new SampledEachUriStatBo();
        final String uri = getUri(eachUriStatBoList);
        sampledEachUriStatBo.setUri(uri);

        List<UriStatHistogram> totalUriStatHistogramList = eachUriStatBoList.stream().map(EachUriStatBo::getTotalHistogram).filter(h -> Objects.nonNull(h)).collect(Collectors.toList());
        SampledUriStatHistogramBo sampledTotalUriStatHistogramBo = create(timestamp, totalUriStatHistogramList);
        sampledEachUriStatBo.setTotalSampledUriStatHistogramBo(sampledTotalUriStatHistogramBo);

        List<UriStatHistogram> failedUriStatHistogramList = eachUriStatBoList.stream().map(EachUriStatBo::getFailedHistogram).filter(h -> Objects.nonNull(h)).collect(Collectors.toList());
        SampledUriStatHistogramBo failedUriStatHistogramBo = create(timestamp, failedUriStatHistogramList);
        sampledEachUriStatBo.setFailedSampledUriStatHistogramBo(failedUriStatHistogramBo);

        return sampledEachUriStatBo;
    }

    private String getUri(List<EachUriStatBo> eachUriStatBoList) {
        EachUriStatBo representative = ListUtils.getFirst(eachUriStatBoList);
        return representative.getUri();
    }

    private SampledUriStatHistogramBo create(long timestamp, List<UriStatHistogram> uriStatHistogramList) {
        if (CollectionUtils.isEmpty(uriStatHistogramList)) {
            return createEmptySampledUriStatHistogramBo(timestamp);
        }

        final List<Integer> countList = uriStatHistogramList.stream().map(UriStatHistogram::getCount).collect(Collectors.toList());
        final List<Long> maxElapsedTimeList = uriStatHistogramList.stream().map(UriStatHistogram::getMax).collect(Collectors.toList());
        final List<Double> avgElapsedTimeList = uriStatHistogramList.stream().map(UriStatHistogram::getAvg).collect(Collectors.toList());

        long totalElapsedTime = 0;

        int bucketSize = UriStatHistogramBucket.values().length;
        Map<UriStatHistogramBucket, List<Long>> uriStatHistogramBucketIntegerMap = new HashMap<>();
        for (UriStatHistogram uriStatHistogram : uriStatHistogramList) {
            int count = uriStatHistogram.getCount();
            totalElapsedTime += count * uriStatHistogram.getAvg();

            int[] timestampHistogram = uriStatHistogram.getTimestampHistogram();
            for (int i = 0; i < bucketSize; i++) {

                UriStatHistogramBucket valueByIndex = UriStatHistogramBucket.getValueByIndex(i);
                List<Long> longs = uriStatHistogramBucketIntegerMap.computeIfAbsent(valueByIndex, f -> new ArrayList<>());
                longs.add((long) timestampHistogram[i]);
            }
        }

        Map<UriStatHistogramBucket, Long> uriStatHistogramBucketIntegerMap2 = new HashMap<>();
        for (UriStatHistogramBucket value : UriStatHistogramBucket.values()) {
            List<Long> longs = uriStatHistogramBucketIntegerMap.get(value);
            if (CollectionUtils.hasLength(longs)) {
                long tValue = 0;
                for (Long aLong : longs) {
                    tValue += aLong;
                }
                uriStatHistogramBucketIntegerMap2.put(value, tValue);
            } else {
                uriStatHistogramBucketIntegerMap2.put(value, 0L);
            }
        }

        AgentStatPoint<Integer> intPoint = createIntPoint(timestamp, countList);
        AgentStatPoint<Long> longPoint1 = createLongPoint(timestamp, maxElapsedTimeList);
        AgentStatPoint<Double> doublePoint = createDoublePoint(timestamp, avgElapsedTimeList);

        SampledUriStatHistogramBo sampledUriStatHistogramBo = new SampledUriStatHistogramBo(intPoint, longPoint1, doublePoint, uriStatHistogramBucketIntegerMap2, totalElapsedTime);
        return sampledUriStatHistogramBo;
    }

    private SampledUriStatHistogramBo createEmptySampledUriStatHistogramBo(long timestamp) {
        AgentStatPoint<Integer> emptyIntegerPoint = createIntPoint(timestamp, Collections.emptyList());
        AgentStatPoint<Long> emptyLongPoint = createLongPoint(timestamp, Collections.emptyList());
        AgentStatPoint<Double> emptyDoublePoint = createDoublePoint(timestamp, Collections.emptyList());

        SampledUriStatHistogramBo sampledUriStatHistogramBo = new SampledUriStatHistogramBo(emptyIntegerPoint, emptyLongPoint, emptyDoublePoint, EMPTY_URI_STAT_HISTOGRAM_MAP, 0L);
        return sampledUriStatHistogramBo;
    }

    private AgentStatPoint<Double> newDoublePoint(long timestamp, List<JvmGcDetailedBo> dataPoints, ToDoubleFunction<JvmGcDetailedBo> filter) {
        List<Double> filteredList = doubleFilter(dataPoints, filter);
        return createDoublePoint(timestamp, filteredList);
    }

    private List<Double> doubleFilter(List<JvmGcDetailedBo> dataPoints, ToDoubleFunction<JvmGcDetailedBo> filter) {
        final List<Double> result = new ArrayList<>(dataPoints.size());
        for (JvmGcDetailedBo jvmGcDetailedBo : dataPoints) {
            final double apply = filter.applyAsDouble(jvmGcDetailedBo);
            if (apply != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                final double percentage = apply * 100;
                result.add(percentage);
            }
        }
        return result;
    }

    private AgentStatPoint<Integer> createIntPoint(long timestamp, List<Integer> values) {
        if (values.isEmpty()) {
            return SampledUriStatHistogramBo.UNCOLLECTED_INT_VALUE_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                INTEGER_DOWN_SAMPLER.sampleMin(values),
                INTEGER_DOWN_SAMPLER.sampleMax(values),
                INTEGER_DOWN_SAMPLER.sampleAvg(values, 0),
                INTEGER_DOWN_SAMPLER.sampleSum(values));

    }


    private AgentStatPoint<Long> createLongPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return SampledJvmGcDetailed.UNCOLLECTED_VALUE_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                LONG_DOWN_SAMPLER.sampleMin(values),
                LONG_DOWN_SAMPLER.sampleMax(values),
                LONG_DOWN_SAMPLER.sampleAvg(values, 0),
                LONG_DOWN_SAMPLER.sampleSum(values));

    }

    private AgentStatPoint<Double> createDoublePoint(long timestamp, List<Double> values) {
        if (values.isEmpty()) {
            return SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                DOUBLE_DOWN_SAMPLER.sampleMin(values),
                DOUBLE_DOWN_SAMPLER.sampleMax(values),
                DOUBLE_DOWN_SAMPLER.sampleAvg(values),
                DOUBLE_DOWN_SAMPLER.sampleSum(values));

    }

}

