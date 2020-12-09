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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;
import com.navercorp.pinpoint.web.vo.stat.SampledUriStatHistogramBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class AgentUriStatChart implements StatChart {

    private static final String UNCOLLECTED_STRING = null;

//    List<SampledEachUriStatBo>

    private final String uri;

    private final AgentUriChartGroup agentUriChartGroup;
    private final AgentUriChartGroup failedAgentUriChartGroup;

    public AgentUriStatChart(TimeWindow timeWindow, List<SampledEachUriStatBo> sampledEachUriStatBoList) {
        SampledEachUriStatBo representative = ListUtils.getFirst(sampledEachUriStatBoList);
        if (representative == null) {
            this.uri = UNCOLLECTED_STRING;
        } else {
            this.uri = representative.getUri();
        }

        List<SampledUriStatHistogramBo> total = sampledEachUriStatBoList.stream().map(SampledEachUriStatBo::getTotalSampledUriStatHistogramBo).collect(Collectors.toList());
        this.agentUriChartGroup = new AgentUriChartGroup(timeWindow, total);

        List<SampledUriStatHistogramBo> failed = sampledEachUriStatBoList.stream().map(SampledEachUriStatBo::getFailedSampledUriStatHistogramBo).collect(Collectors.toList());
        this.failedAgentUriChartGroup = new AgentUriChartGroup(timeWindow, failed);
    }

    public String getUri() {
        return uri;
    }

    public long getTotalCount() {
        return agentUriChartGroup.totalCount;
    }

    public double getAvg() {
        return agentUriChartGroup.avg;
    }

    public long getMax() {
        return agentUriChartGroup.max;
    }

    @Override
    public StatChartGroup getCharts() {
        return agentUriChartGroup;
    }

//    public StatChartGroup getFailedCharts() {
//        return failedAgentUriChartGroup;
//    }

    public static class AgentUriChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;


        private long totalCount;
        private long max;
        private double avg;

        private final Map<ChartType, Chart<? extends Point>> totalCharts;


        private AgentUriChartGroup(TimeWindow timeWindow, List<SampledUriStatHistogramBo> total) {
            this.timeWindow = timeWindow;
            this.totalCharts = newChart(total);
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }


        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return totalCharts;
        }

        public enum AgentUriChartType implements AgentChartType {
            COUNT,
            AVG,
            MAX,
            HISTOGRAM_BUCKET;
        }

        public enum AgentUriHistogramType implements AgentChartType {
            HISTOGRAM_BUCKET;

            @Override
            public String[] getSchema() {
                UriStatHistogramBucket[] values = UriStatHistogramBucket.values();
                int length = values.length;
                String[] strings = new String[length];


                for (int i = 0; i < length; i++) {
                    strings[i] = values[i].getDesc();
                }

                return strings;
            }

        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledUriStatHistogramBo> sampledEachUriStatBoList) {
            long totalCount = 0;
            long max = 0;
            double avg = 0l;


            for (SampledUriStatHistogramBo sampledUriStatHistogramBo : sampledEachUriStatBoList) {
                Integer maxYVal = sampledUriStatHistogramBo.getTotalCount().getMaxYVal();
                totalCount += maxYVal;
            }

            for (SampledUriStatHistogramBo sampledUriStatHistogramBo : sampledEachUriStatBoList) {
                long maxValue = sampledUriStatHistogramBo.getMaxElapsed().getMaxYVal();
                max = Math.max(max, maxValue);
            }

            long totalElasepdTime = 0;
            for (SampledUriStatHistogramBo sampledUriStatHistogramBo : sampledEachUriStatBoList) {
                totalElasepdTime += sampledUriStatHistogramBo.getTotalElapsedTime();
            }

            if (totalCount != 0) {
                avg = totalElasepdTime / totalCount;
            }

            this.avg = avg;
            this.max = max;
            this.totalCount = totalCount;



            Chart<AgentStatPoint<Integer>> totalCountChart = newIntChart(sampledEachUriStatBoList, SampledUriStatHistogramBo::getTotalCount);
            Chart<AgentStatPoint<Long>> maxElapsedChart = newLongChart(sampledEachUriStatBoList, SampledUriStatHistogramBo::getMaxElapsed);
            Chart<AgentStatPoint<Double>> avgChart = newDoubleChart(sampledEachUriStatBoList, SampledUriStatHistogramBo::getAvg);

            Map<ChartType, Chart<? extends Point>> totalCharts = new HashMap<>();
            totalCharts.put(AgentUriChartType.COUNT, totalCountChart);
            totalCharts.put(AgentUriChartType.AVG, avgChart);
            totalCharts.put(AgentUriChartType.MAX, maxElapsedChart);


            List<Long> timeStampList = new ArrayList<>();
            for (long timestamp : this.timeWindow) {
                timeStampList.add(timestamp);
            }


            List<UriStatHistogramPoint> histogramPoints = new ArrayList<>();
            int size = sampledEachUriStatBoList.size();
            for (int i = 0; i < size; i++) {
                SampledUriStatHistogramBo sampledUriStatHistogramBo = sampledEachUriStatBoList.get(i);

                Map<UriStatHistogramBucket, Long> uriStatHistogramBucketIntegerMap = sampledUriStatHistogramBo.getUriStatHistogramBucketIntegerMap();

                long[] yVal = new long[UriStatHistogramBucket.values().length];
                for (UriStatHistogramBucket value : UriStatHistogramBucket.values()) {
                    Long aLong = uriStatHistogramBucketIntegerMap.get(value);
                    yVal[value.getIndex()] += aLong;
                }

                long xVal = sampledUriStatHistogramBo.getMaxElapsed().getXVal();

                UriStatHistogramPoint histogramPoint = new UriStatHistogramPoint(xVal, yVal);
                histogramPoints.add(histogramPoint);
            }

            totalCharts.put(AgentUriHistogramType.HISTOGRAM_BUCKET, newHistogramPointChart(histogramPoints));

            return totalCharts;
        }

        //

        private Chart<AgentStatPoint<Integer>> newIntChart(List<SampledUriStatHistogramBo> sampledEachUriStatBoList, Function<SampledUriStatHistogramBo, AgentStatPoint<Integer>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Integer>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledUriStatHistogramBo.UNCOLLECTED_INT_VALUE_POINT_CREATOR);
            return builder.build(sampledEachUriStatBoList, function);
        }

        private Chart<AgentStatPoint<Long>> newLongChart(List<SampledUriStatHistogramBo> sampledEachUriStatBoList, Function<SampledUriStatHistogramBo, AgentStatPoint<Long>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledEachUriStatBo.UNCOLLECTED_LONG_POINT_CREATOR);
            return builder.build(sampledEachUriStatBoList, function);
        }

        private Chart<AgentStatPoint<Double>> newDoubleChart(List<SampledUriStatHistogramBo> sampledEachUriStatBoList, Function<SampledUriStatHistogramBo, AgentStatPoint<Double>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Double>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledEachUriStatBo.UNCOLLECTED_POINT_CREATOR);
            return builder.build(sampledEachUriStatBoList, function);
        }

        private Chart<UriStatHistogramPoint> newHistogramPointChart(List<UriStatHistogramPoint> histogramPointList) {
            TimeSeriesChartBuilder<UriStatHistogramPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_POINT_CREATOR);
            return builder.build(histogramPointList);
        }

    }


    public static final long[] UNCOLLECTED = new long[]{0, 0, 0, 0, 0, 0, 0, 0};
    public static final Point.UncollectedPointCreator<UriStatHistogramPoint> UNCOLLECTED_POINT_CREATOR = new Point.UncollectedPointCreator<UriStatHistogramPoint>() {
        @Override
        public UriStatHistogramPoint createUnCollectedPoint(long xVal) {
            return new UriStatHistogramPoint(xVal, UNCOLLECTED);
        }
    };

    private UriStatHistogramPoint createHistogramPoint(long timestamp, long[] values) {
        return new UriStatHistogramPoint(
                timestamp, values);
    }

}
