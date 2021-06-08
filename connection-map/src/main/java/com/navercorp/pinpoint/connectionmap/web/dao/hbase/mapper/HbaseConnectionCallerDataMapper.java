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

package com.navercorp.pinpoint.connectionmap.web.dao.hbase.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionType;
import com.navercorp.pinpoint.connectionmap.common.proto.Direction;
import com.navercorp.pinpoint.connectionmap.common.util.InetFamily;
import com.navercorp.pinpoint.connectionmap.common.util.Offset;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
@Component
public class HbaseConnectionCallerDataMapper implements RowMapper<ConnectionStatVo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ConnectionStatVo mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();


        Offset offset = new Offset(0);
        byte inetFamilyByte = rowKey[offset.getAndAdd(1)];
        final InetFamily inetFamily = InetFamily.getFamily(inetFamilyByte);
        if (inetFamily == null) {
            logger.warn("Invalid connectionFamily found.");
            return null;
        }

        final byte[] dstAddress = Arrays.copyOfRange(rowKey, offset.get(), offset.addAndGet(inetFamily.getAddressSize()));

        ConnectionStatVo.Builder builder = new ConnectionStatVo.Builder();
        builder.inetFamily(inetFamily);
        builder.dstIp(dstAddress);
        builder.direction(Direction.INCOMING);

        long reverseStartTime = BytesUtils.bytesToLong(rowKey, offset.get());
        long timeslot = TimeUtils.recoveryTimeMillis(reverseStartTime);
        builder.timestamp(timeslot);

        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            if (CellUtil.matchingFamily(cell, ConnectionMapHbaseColumnFamily.CONNECTION_STAT_CALLER.getName())) {

                byte[] qualifierArray = cell.getQualifierArray();
                Buffer qualifierBuffer = new OffsetFixedBuffer(qualifierArray, cell.getQualifierOffset(), cell.getQualifierLength());

                String agentId = qualifierBuffer.readPrefixedString();
                builder.agentId(agentId);

                byte connectionTypeByte = qualifierBuffer.readByte();
                ConnectionType connectionType = ConnectionType.forNumber(connectionTypeByte);
                builder.connectionType(connectionType);

                int dstPort = qualifierBuffer.readVInt();
                builder.dstPort(dstPort);

                Offset srcAddressOffset = new Offset(qualifierBuffer.getOffset());
                final byte[] srcAddress = Arrays.copyOfRange(qualifierArray, srcAddressOffset.get(), srcAddressOffset.addAndGet(inetFamily.getAddressSize()));
                builder.srcIp(srcAddress);

                qualifierBuffer.setOffset(srcAddressOffset.get());

                int srcPort = qualifierBuffer.readVInt();
                builder.srcPort(srcPort);

                int pid = qualifierBuffer.readVInt();
                builder.pid(pid);

                Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                long monotonicSentBytes = valueBuffer.readVLong();
                builder.monotonicSentBytes(monotonicSentBytes);

                long sentBytes = valueBuffer.readVLong();
                builder.sentBytes(sentBytes);

                long monotonicRecvBytes = valueBuffer.readVLong();
                builder.monotonicRecvBytes(monotonicRecvBytes);

                long recvBytes = valueBuffer.readVLong();
                builder.recvBytes(recvBytes);

                long monotonicRetransmits = valueBuffer.readVLong();
                builder.monotonicRetransmits(new Long(monotonicRetransmits).intValue());

                long lastRetransmits = valueBuffer.readVLong();
                builder.lastRetransmits(new Long(lastRetransmits).intValue());
            }
        }

        return builder.build();
    }

}


