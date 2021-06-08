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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
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
public class HbaseConnectionSelfDataMapper implements RowMapper<ConnectionStatVo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ConnectionStatVo mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();
        String agentId = BytesUtils.safeTrim(BytesUtils.toString(rowKey, 0, PinpointConstants.AGENT_ID_MAX_LEN));

        int pid = BytesUtils.bytesToInt(rowKey, PinpointConstants.AGENT_ID_MAX_LEN);

        long reverseStartTime = BytesUtils.bytesToLong(rowKey, HbaseTableConstants.AGENT_ID_MAX_LEN + 4);
        long timeslot = TimeUtils.recoveryTimeMillis(reverseStartTime);

        ConnectionStatVo.Builder builder = new ConnectionStatVo.Builder();
        builder.pid(pid);
        builder.agentId(agentId);
        builder.direction(Direction.LOCAL);
        builder.timestamp(timeslot);

        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            if (CellUtil.matchingFamily(cell, ConnectionMapHbaseColumnFamily.CONNECTION_STAT_SELF.getName())) {

                byte[] qualifierArray = cell.getQualifierArray();
                Offset qualifierOffset = new Offset(cell.getQualifierOffset());

                byte bConnectionType = qualifierArray[qualifierOffset.getAndAdd(1)];
                ConnectionType connectionType = ConnectionType.forNumber(bConnectionType);
                builder.connectionType(connectionType);

                byte inetFamilyBytes = qualifierArray[qualifierOffset.getAndAdd(1)];
                final InetFamily inetFamily = InetFamily.getFamily(inetFamilyBytes);
                if (inetFamily == null) {
                    logger.warn("Invalid connectionFamily found.");
                    return null;
                }
                builder.inetFamily(inetFamily);

                final byte[] srcAddress = Arrays.copyOfRange(qualifierArray, qualifierOffset.get(), qualifierOffset.addAndGet(inetFamily.getAddressSize()));
                builder.srcIp(srcAddress);

                final int srcPort = BytesUtils.bytesToInt(qualifierArray, qualifierOffset.getAndAdd(4));
                builder.srcPort(srcPort);

                final byte[] dstAddress = Arrays.copyOfRange(qualifierArray, qualifierOffset.get(), qualifierOffset.addAndGet(inetFamily.getAddressSize()));
                builder.dstIp(dstAddress);

                final int dstPort = BytesUtils.bytesToInt(qualifierArray, qualifierOffset.getAndAdd(4));
                builder.dstPort(dstPort);

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

