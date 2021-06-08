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
import com.navercorp.pinpoint.connectionmap.common.util.InetFamily;
import com.navercorp.pinpoint.connectionmap.common.util.Offset;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.web.dao.ConnectionStatCallerDao;
import com.navercorp.pinpoint.connectionmap.web.dao.hbase.mapper.HbaseConnectionCallerDataResultsExtractor;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.temporal.ValueRange;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseConnectionStatCallerDao implements ConnectionStatCallerDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int SCANNER_CACHE_SIZE = 20;

    private final ResultsExtractor<List<ConnectionStatVo>> connectionCallerDataResultsExtractor = new HbaseConnectionCallerDataResultsExtractor();

    private final HbaseOperations2 hbaseOperations2;

    private final ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCaller> descriptor;

    public HbaseConnectionStatCallerDao(HbaseOperations2 hbaseOperations2, ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCaller> descriptor) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public List<ConnectionStatVo> getConnectionStatVos(String dstIpAddress, ValueRange range) {
        try {
            InetAddress address = InetAddress.getByName(dstIpAddress);
            byte[] bAddress = address.getAddress();

            return getConnectionStatVos(bAddress, range);
        } catch (UnknownHostException e) {
            logger.warn("Failed to execute getConnectionStatVos. Cause:{}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }


    @Override
    public List<ConnectionStatVo> getConnectionStatVos(byte[] dstIpAddress, ValueRange range) {
        try {
            Scan scan = new Scan();
            scan.setMaxVersions(1);
            scan.setCaching(SCANNER_CACHE_SIZE);

            scan.withStartRow(createRowKey(dstIpAddress, range.getMaximum()));
            scan.withStopRow(createRowKey(dstIpAddress, range.getMinimum()));
            scan.addFamily(descriptor.getColumnFamilyName());

            TableName connectionStatCallerTable = descriptor.getTableName();
            List<ConnectionStatVo> connectionStatVoList = this.hbaseOperations2.find(connectionStatCallerTable, scan, connectionCallerDataResultsExtractor);

            logger.debug("connectionStatVoList found. {}", connectionStatVoList);

            return connectionStatVoList;
        } catch (UnknownHostException e) {
            logger.warn("Failed to execute getConnectionStatVos. Cause:{}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private byte[] createRowKey(byte[] dstIpAddress, long timestamp) throws UnknownHostException {
        final InetFamily inetFamily = InetFamily.getFamilyByAddressSize(dstIpAddress.length);
        if (inetFamily == null) {
            throw new IllegalArgumentException("invalid address type. length:" + dstIpAddress.length);
        }

        byte[] rowKey = new byte[1 + dstIpAddress.length + LONG_BYTE_LENGTH];

        Offset offset = new Offset(0);
        rowKey[offset.get()] = inetFamily.getByteNumber();
        BytesUtils.writeBytes(rowKey, offset.addAndGet(1), dstIpAddress);

        long reverseStartTimestamp = TimeUtils.reverseTimeMillis(timestamp);
        BytesUtils.writeLong(reverseStartTimestamp, rowKey, offset.addAndGet(dstIpAddress.length));

        return rowKey;
    }

}
