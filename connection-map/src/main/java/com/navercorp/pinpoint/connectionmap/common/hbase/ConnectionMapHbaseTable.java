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

package com.navercorp.pinpoint.connectionmap.common.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseTable;

/**
 * @author Taejin Koo
 */
public enum ConnectionMapHbaseTable {

    CONNECTION_STAT_DATA("ConnectionStatData"),
    PID_STAT_DATA("PidStatData"),

    CONNECTION_STAT_CALLEE("NetworkConnectionStatisticsCallee"),
    CONNECTION_STAT_CALLER("NetworkConnectionStatisticsCaller"),
    CONNECTION_STAT_SELF("NetworkConnectionStatisticsSelf");

    private final String name;
    private final boolean mustIncluded;

    ConnectionMapHbaseTable(String name) {
        this(name, true);
    }

    ConnectionMapHbaseTable(String name, boolean mustIncluded) {
        this.name = name;
        this.mustIncluded = mustIncluded;
    }

    public String getName() {
        return name;
    }

    public boolean isMustIncluded() {
        return mustIncluded;
    }

}
