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
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.vo.PidSetVo;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Component
public class HbasePidStatDataMapper implements RowMapper<PidSetVo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public PidSetVo mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();

        final Buffer row = new FixedBuffer(rowKey);
        String agentId = row.read2PrefixedString();

        final long timestamp = TimeUtils.recoveryTimeMillis(row.readLong());

        Set<Integer> pidSet = new HashSet<Integer>();

        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            if (CellUtil.matchingFamily(cell, ConnectionMapHbaseColumnFamily.PID_STAT_DATA.getName())) {
                Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());

                while (valueBuffer.hasRemaining()) {
                    int pid = valueBuffer.readVInt();
                    pidSet.add(pid);
                }
            }
        }

        PidSetVo pidHbaseVo = new PidSetVo(agentId, timestamp, pidSet);
        return pidHbaseVo;
    }

}

