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
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.collector.dao.ConnectionStatCallerDao;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapTableDescriptor;
import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionType;
import com.navercorp.pinpoint.connectionmap.common.util.InetFamily;
import com.navercorp.pinpoint.connectionmap.common.util.Offset;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseConnectionStatCallerDao implements ConnectionStatCallerDao {

    private final HbaseOperations2 hbaseTemplate;

    private final ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCaller> descriptor;

    private final AcceptedTimeService acceptedTimeService;

    public HbaseConnectionStatCallerDao(HbaseOperations2 hbaseTemplate, ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCaller> descriptor, AcceptedTimeService acceptedTimeService) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
    }

    @Override
    public void insert(ConnectionStatVo connectionStat) {
        byte[] rowKey = createRowKey(connectionStat);

        final Put put = new Put(rowKey);

        byte[] qualifier = createQualifier(connectionStat);
        byte[] value = createValue(connectionStat);

        put.addColumn(descriptor.getColumnFamilyName(), qualifier, value);

        hbaseTemplate.put(descriptor.getTableName(), put);
    }

    private byte[] createRowKey(ConnectionStatVo connectionStat) {
        long acceptedTime = acceptedTimeService.getAcceptedTime();

        final InetFamily inetFamily = connectionStat.getInetFamily();
        if (inetFamily == null) {
            throw new IllegalArgumentException("inetFamily is null");
        }

        byte[] bDstIp = connectionStat.getDstIp();
        if (inetFamily.getAddressSize() != bDstIp.length) {
            throw new IllegalArgumentException("Failed to create rowkey. Cause : srcIpSize is invalid. size:" + bDstIp.length);
        }
        int dstIpSize = bDstIp.length;

        byte[] rowKey = new byte[1 + dstIpSize + LONG_BYTE_LENGTH];

        Offset offset = new Offset(0);
        rowKey[offset.get()] = inetFamily.getByteNumber();
        BytesUtils.writeBytes(rowKey, offset.addAndGet(1), bDstIp);
        BytesUtils.writeLong(TimeUtils.reverseTimeMillis(acceptedTime), rowKey, offset.addAndGet(dstIpSize));

        return rowKey;
    }

    private byte[] createQualifier(final ConnectionStatVo connectionStat) {
        final Buffer buffer = new AutomaticBuffer(18);

        buffer.putPrefixedString(connectionStat.getAgentId());

        ConnectionType connectionType = connectionStat.getConnectionType();
        byte bConnectionType = new Integer(connectionType.getNumber()).byteValue();
        buffer.putByte(bConnectionType);

        int dstPort = connectionStat.getDstPort();
        buffer.putVInt(dstPort);

        byte[] srcIp = connectionStat.getSrcIp();
        buffer.putBytes(srcIp);
        int srcPort = connectionStat.getSrcPort();
        buffer.putVInt(srcPort);

        int pid = connectionStat.getPid();
        buffer.putVInt(pid);

        return buffer.getBuffer();
    }

    private byte[] createValue(final ConnectionStatVo connectionStat) {
        final Buffer buffer = new AutomaticBuffer(18);

        long monotonicSentBytes = connectionStat.getMonotonicSentBytes();
        long sentBytes = connectionStat.getSentBytes();
        buffer.putVLong(monotonicSentBytes);
        buffer.putVLong(sentBytes);

        long monotonicRecvBytes = connectionStat.getMonotonicRecvBytes();
        long recvBytes = connectionStat.getRecvBytes();
        buffer.putVLong(monotonicRecvBytes);
        buffer.putVLong(recvBytes);

        int monotonicRetransmits = connectionStat.getMonotonicRetransmits();
        int lastRetransmits = connectionStat.getLastRetransmits();
        buffer.putVLong(monotonicRetransmits);
        buffer.putVLong(lastRetransmits);

        return buffer.getBuffer();
    }

}
