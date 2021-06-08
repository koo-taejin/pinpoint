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

package com.navercorp.pinpoint.connectionmap.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapTableDescriptor;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.web.dao.ConnectionStatSelfDao;
import com.navercorp.pinpoint.connectionmap.web.dao.hbase.mapper.HbaseConnectionSelfDataResultsExtractor;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.temporal.ValueRange;
import java.util.List;

import static com.navercorp.pinpoint.common.util.BytesUtils.INT_BYTE_LENGTH;
import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseConnectionStatSelfDao implements ConnectionStatSelfDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int SCANNER_CACHE_SIZE = 20;

    private final HbaseOperations2 hbaseOperations2;

    private final ResultsExtractor<List<ConnectionStatVo>> connectionSelfDataResultsExtractor = new HbaseConnectionSelfDataResultsExtractor();

    private final ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatSelf> descriptor;

    public HbaseConnectionStatSelfDao(HbaseOperations2 hbaseOperations2, ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatSelf> descriptor) {
        this.hbaseOperations2 = hbaseOperations2;
        this.descriptor = descriptor;
    }

    @Override
    public List<ConnectionStatVo> getConnectionStatVos(String agentId, int pid, ValueRange range) {
        Scan scan = new Scan();
        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHE_SIZE);

        scan.withStartRow(createRowKey(agentId, pid, range.getMaximum()));
        scan.withStopRow(createRowKey(agentId, pid, range.getMinimum()));
        scan.addFamily(descriptor.getColumnFamilyName());

        TableName agentEventTableName = descriptor.getTableName();
        List<ConnectionStatVo> connectionStatVoList = this.hbaseOperations2.find(agentEventTableName, scan, connectionSelfDataResultsExtractor);
        logger.debug("connectionStatVoList found. {}", connectionStatVoList);

        return connectionStatVoList;
    }

    private byte[] createRowKey(String agentId, int pid, long timestamp) {
        byte[] agentIdBytes = Bytes.toBytes(agentId);

        byte[] rowKey = new byte[24 + INT_BYTE_LENGTH + LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, agentIdBytes);
        BytesUtils.writeInt(pid, rowKey, 24);

        long reverseStartTimestamp = TimeUtils.reverseTimeMillis(timestamp);

        BytesUtils.writeLong(reverseStartTimestamp, rowKey, 24 + INT_BYTE_LENGTH);

        return rowKey;
    }


}
