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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.collector.dao.PidBulkWriter;
import com.navercorp.pinpoint.connectionmap.collector.dao.PidIncrementerDao;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapTableDescriptor;
import com.navercorp.pinpoint.connectionmap.common.vo.PidSetVo;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Repository
public class HbasePidBulkWriter implements PidBulkWriter {

    private final HbaseOperations2 hbaseTemplate;

    private final ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.PidStatData> descriptor;

    private final PidIncrementerDao pidIncrementerDao;


    public HbasePidBulkWriter(HbaseOperations2 hbaseTemplate, ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.PidStatData> descriptor, PidIncrementerDao pidIncrementerDao) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.pidIncrementerDao = Objects.requireNonNull(pidIncrementerDao, "pidIncrementerDao");
    }

    @Override
    public void flush() {
        List<PidSetVo> oldestPidSetList = pidIncrementerDao.getOldestPidSetList();

        for (PidSetVo pidSetVo : oldestPidSetList) {
            byte[] rowKey = createRowKey(pidSetVo);
            byte[] value = createValue(pidSetVo);

            final Put put = new Put(rowKey);
            put.addColumn(descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_STRING, value);

            hbaseTemplate.put(descriptor.getTableName(), put);
        }
    }

    private static byte[] createRowKey(PidSetVo pidSetVo) {
        String agentId = pidSetVo.getAgentId();
        long timestamp = pidSetVo.getTimestamp();

        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);

        final Buffer buffer = new AutomaticBuffer(2 + agentIdBytes.length + 8);

        buffer.putShort((short) agentIdBytes.length);
        buffer.putBytes(agentIdBytes);

        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }


    private static byte[] createValue(PidSetVo pidSetVo) {
        Set<Integer> pidSet = pidSetVo.getPidSet();

        final Buffer buffer = new AutomaticBuffer(4 * pidSet.size());

        for (Integer pid : pidSet) {
            buffer.putVInt(pid);
        }

        return buffer.getBuffer();
    }

}
