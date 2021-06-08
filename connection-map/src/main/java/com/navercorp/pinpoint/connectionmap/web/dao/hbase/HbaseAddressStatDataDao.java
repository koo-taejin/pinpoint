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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapTableDescriptor;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalAddressesVo;
import com.navercorp.pinpoint.connectionmap.web.dao.AddressStatDataDao;
import com.navercorp.pinpoint.connectionmap.web.dao.hbase.mapper.HbaseAddressStatDataResultsExtractor;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseAddressStatDataDao implements AddressStatDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int SCANNER_CACHE_SIZE = 20;


    private final HbaseOperations2 hbaseOperations2;

    private final ResultsExtractor<List<LocalAddressesVo>> hbaseAddressStatDataResultsExtractor = new HbaseAddressStatDataResultsExtractor();

    private final ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatData> descriptor;

    public HbaseAddressStatDataDao(HbaseOperations2 hbaseOperations2, ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatData> descriptor) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public List<LocalAddressesVo> getLocalAddresses(String agentId, ValueRange range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        Scan scan = new Scan();
        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHE_SIZE);

        scan.withStartRow(createRowKey(agentId, range.getMaximum()));
        scan.withStopRow(createRowKey(agentId, range.getMinimum()));
        scan.addFamily(descriptor.getColumnFamilyName());

        TableName agentEventTableName = descriptor.getTableName();
        List<LocalAddressesVo> localAddressesVoList = this.hbaseOperations2.find(agentEventTableName, scan, hbaseAddressStatDataResultsExtractor);
        logger.debug("localAddressesVoList found. {}", localAddressesVoList);

        return localAddressesVoList;
    }

    private byte[] createRowKey(String agentId, long timestamp) {
        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);

        final Buffer buffer = new AutomaticBuffer(2 + agentIdBytes.length + 8);

        buffer.putShort((short) agentIdBytes.length);
        buffer.putBytes(agentIdBytes);

        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }

}
