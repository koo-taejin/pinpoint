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

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;

import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ConnectionMapHbaseColumnFamily {

    public static final ConnectionStatData CONNECTION_STAT_DATA = new ConnectionStatData(ConnectionMapHbaseTable.CONNECTION_STAT_DATA, Bytes.toBytes("C"));

    public static class ConnectionStatData extends ConnectionMapHbaseColumnFamily {
        public byte[] QUALIFIER_STRING = Bytes.toBytes("Address");

        private ConnectionStatData(ConnectionMapHbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final PidStatData PID_STAT_DATA = new PidStatData(ConnectionMapHbaseTable.PID_STAT_DATA, Bytes.toBytes("P"));

    public static class PidStatData extends ConnectionMapHbaseColumnFamily {
        public byte[] QUALIFIER_STRING = Bytes.toBytes("Pid");

        private PidStatData(ConnectionMapHbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }


    public static final ConnectionStatCallee CONNECTION_STAT_CALLEE = new ConnectionStatCallee(ConnectionMapHbaseTable.CONNECTION_STAT_CALLEE, Bytes.toBytes("NCS"));

    public static class ConnectionStatCallee extends ConnectionMapHbaseColumnFamily {
        private ConnectionStatCallee(ConnectionMapHbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final ConnectionStatCaller CONNECTION_STAT_CALLER = new ConnectionStatCaller(ConnectionMapHbaseTable.CONNECTION_STAT_CALLER, Bytes.toBytes("NCS"));

    public static class ConnectionStatCaller extends ConnectionMapHbaseColumnFamily {
        private ConnectionStatCaller(ConnectionMapHbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    public static final ConnectionStatSelf CONNECTION_STAT_SELF = new ConnectionStatSelf(ConnectionMapHbaseTable.CONNECTION_STAT_SELF, Bytes.toBytes("NCS"));

    public static class ConnectionStatSelf extends ConnectionMapHbaseColumnFamily {
        private ConnectionStatSelf(ConnectionMapHbaseTable hBaseTable, byte[] columnFamilyName) {
            super(hBaseTable, columnFamilyName);
        }
    }

    private final ConnectionMapHbaseTable hBaseTable;
    private final byte[] columnFamilyName;

    ConnectionMapHbaseColumnFamily(ConnectionMapHbaseTable hBaseTable, byte[] columnFamilyName) {
        this.hBaseTable = Objects.requireNonNull(hBaseTable, "hBaseTable");
        Assert.isTrue(ArrayUtils.hasLength(columnFamilyName), "columnFamilyName must not be empty");
        this.columnFamilyName = columnFamilyName;
    }

    public ConnectionMapHbaseTable getTable() {
        return hBaseTable;
    }

    public byte[] getName() {
        return columnFamilyName;
    }
}
