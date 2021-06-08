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

package com.navercorp.pinpoint.connectionmap.collector.dao.hbase;

import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.connectionmap.collector.dao.PidIncrementerDao;
import com.navercorp.pinpoint.connectionmap.common.vo.PidSetVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Repository
public class HbasePidIncrementerDao implements PidIncrementerDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TimeSlot timeSlot = new DefaultTimeSlot();

    private ConcurrentSkipListMap<Long, Map<String, ConcurrentSkipListSet<Integer>>> timeSlotBasePidSetMap = new ConcurrentSkipListMap<>();

    private final int flushMinutes;

    private final AcceptedTimeService acceptedTimeService;

    public HbasePidIncrementerDao(AcceptedTimeService acceptedTimeService) {
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.flushMinutes = 3;
    }

    @Override
    public void increment(String agentId, int pid) {
        long acceptedTime = acceptedTimeService.getAcceptedTime();

        long timeSlot = this.timeSlot.getTimeSlot(acceptedTime);

        Map<String, ConcurrentSkipListSet<Integer>> pidSetMap = timeSlotBasePidSetMap.get(timeSlot);
        if (pidSetMap == null) {
            timeSlotBasePidSetMap.putIfAbsent(timeSlot, new ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>>());
            pidSetMap = timeSlotBasePidSetMap.get(timeSlot);
        }

        ConcurrentSkipListSet<Integer> pidSet = pidSetMap.get(agentId);
        if (pidSet == null) {
            pidSetMap.putIfAbsent(agentId, new ConcurrentSkipListSet<>());
            pidSet = pidSetMap.get(agentId);
        }

        pidSet.add(pid);
    }

    @Override
    public List<PidSetVo> getOldestPidSetList() {
        NavigableSet<Long> times = timeSlotBasePidSetMap.keySet();

        long currentTimeMillis = System.currentTimeMillis();

        long flushTime = -1;
        for (long time : times) {
            if (time + TimeUnit.MINUTES.toMillis(flushMinutes) < currentTimeMillis) {
                flushTime = time;
                break;
            }
        }

        if (flushTime > 0) {
            List<PidSetVo> pidSetVoList = new ArrayList<>();
            Map<String, ConcurrentSkipListSet<Integer>> remove = timeSlotBasePidSetMap.remove(flushTime);

            for (Map.Entry<String, ConcurrentSkipListSet<Integer>> stringConcurrentSkipListSetEntry : remove.entrySet()) {
                String key = stringConcurrentSkipListSetEntry.getKey();
                ConcurrentSkipListSet<Integer> value = stringConcurrentSkipListSetEntry.getValue();
                PidSetVo pidSetVo = new PidSetVo(key, flushTime, value);

                pidSetVoList.add(pidSetVo);
            }

            return pidSetVoList;
        } else {
            return Collections.emptyList();
        }
    }

}
