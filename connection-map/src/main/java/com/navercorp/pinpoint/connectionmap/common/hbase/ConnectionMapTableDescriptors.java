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

import com.navercorp.pinpoint.common.hbase.TableNameProvider;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ConnectionMapTableDescriptors {


    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatData> getConnectionStatData() {
        return new DefaultConnectionMapTableDescriptor<>(tableNameProvider, ConnectionMapHbaseColumnFamily.CONNECTION_STAT_DATA);
    }

    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.PidStatData> getPidStatData() {
        return new DefaultConnectionMapTableDescriptor<>(tableNameProvider, ConnectionMapHbaseColumnFamily.PID_STAT_DATA);
    }

    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCallee> getConnectionStatCallee() {
        return new DefaultConnectionMapTableDescriptor<>(tableNameProvider, ConnectionMapHbaseColumnFamily.CONNECTION_STAT_CALLEE);
    }

    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCaller> getConnectionStatCaller() {
        return new DefaultConnectionMapTableDescriptor<>(tableNameProvider, ConnectionMapHbaseColumnFamily.CONNECTION_STAT_CALLER);
    }

    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatSelf> getConnectionStatSelf() {
        return new DefaultConnectionMapTableDescriptor<>(tableNameProvider, ConnectionMapHbaseColumnFamily.CONNECTION_STAT_SELF);
    }

    private final TableNameProvider tableNameProvider;

    public ConnectionMapTableDescriptors(TableNameProvider tableNameProvider) {
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

}
