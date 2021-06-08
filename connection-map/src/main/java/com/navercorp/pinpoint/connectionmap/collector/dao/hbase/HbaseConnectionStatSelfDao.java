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
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.collector.dao.ConnectionStatSelfDao;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapTableDescriptor;
import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionType;
import com.navercorp.pinpoint.connectionmap.common.util.InetFamily;
import com.navercorp.pinpoint.connectionmap.common.util.Offset;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.navercorp.pinpoint.common.util.BytesUtils.LONG_BYTE_LENGTH;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseConnectionStatSelfDao implements ConnectionStatSelfDao {

    private final HbaseOperations2 hbaseTemplate;

    private final ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatSelf> descriptor;

    private final AcceptedTimeService acceptedTimeService;

    public HbaseConnectionStatSelfDao(HbaseOperations2 hbaseTemplate, ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatSelf> descriptor, AcceptedTimeService acceptedTimeService) {
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

        final byte[] bAgentId = Bytes.toBytes(connectionStat.getAgentId());

        int pid = connectionStat.getPid();

        Offset offset = new Offset(0);
        byte[] rowKey = new byte[HbaseTableConstants.AGENT_ID_MAX_LEN + BytesUtils.INT_BYTE_LENGTH + LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, offset.get(), bAgentId);
        BytesUtils.writeInt(pid, rowKey, offset.addAndGet(HbaseTableConstants.AGENT_ID_MAX_LEN));
        BytesUtils.writeLong(TimeUtils.reverseTimeMillis(acceptedTime), rowKey, offset.addAndGet(BytesUtils.INT_BYTE_LENGTH));
        return rowKey;
    }

    private byte[] createQualifier(final ConnectionStatVo connectionStat) {
        final Buffer buffer = new AutomaticBuffer(18);

        ConnectionType connectionType = connectionStat.getConnectionType();
        byte bConnectionType = new Integer(connectionType.getNumber()).byteValue();
        buffer.putByte(bConnectionType);

        InetFamily inetFamily = connectionStat.getInetFamily();
        byte bConnectionFamily = inetFamily.getByteNumber();
        buffer.putByte(bConnectionFamily);

        buffer.putBytes(connectionStat.getSrcIp());
        buffer.putInt(connectionStat.getSrcPort());

        buffer.putBytes(connectionStat.getDstIp());
        buffer.putInt(connectionStat.getDstPort());

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
