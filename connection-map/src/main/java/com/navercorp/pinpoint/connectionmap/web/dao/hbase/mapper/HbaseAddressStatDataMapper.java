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
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.util.InetFamily;
import com.navercorp.pinpoint.connectionmap.common.util.Offset;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalAddressesVo;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;

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
public class HbaseAddressStatDataMapper implements RowMapper<LocalAddressesVo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public LocalAddressesVo mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();

        final Buffer row = new FixedBuffer(rowKey);

        String agentId = row.read2PrefixedString();
        TimeUtils.recoveryTimeMillis(row.readLong());

        LocalAddressesVo.Builder builder = new LocalAddressesVo.Builder();
        builder.agentId(agentId);

        Cell[] rawCells = result.rawCells();
        for (Cell cell : rawCells) {
            if (CellUtil.matchingFamily(cell, ConnectionMapHbaseColumnFamily.CONNECTION_STAT_DATA.getName())) {
                Offset offset = new Offset(cell.getValueOffset());

                final byte[] valueArray = cell.getValueArray();
                while (valueArray.length >= offset.get() + 1) {
                    byte inetFamilyByte = valueArray[offset.getAndAdd(1)];
                    final InetFamily inetFamily = InetFamily.getFamily(inetFamilyByte);
                    if (inetFamily == null) {
                        logger.warn("Invalid connectionFamily found.");
                        return null;
                    }
                    final byte[] address = Arrays.copyOfRange(valueArray, offset.get(), offset.addAndGet(inetFamily.getAddressSize()));
                    builder.addLocalIpVo(new LocalIpVo(address));
                }
            }

        }
        LocalAddressesVo build = builder.build();
        return build;
    }

}
