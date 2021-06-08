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
import com.navercorp.pinpoint.common.server.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.connectionmap.collector.dao.AddressStatDataDao;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapHbaseColumnFamily;
import com.navercorp.pinpoint.connectionmap.common.hbase.ConnectionMapTableDescriptor;
import com.navercorp.pinpoint.connectionmap.common.util.InetFamily;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalAddressesVo;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Repository
public class HbaseAddressStatDataDao implements AddressStatDataDao {

    private final TimeSlot timeSlot = new DefaultTimeSlot(TimeUnit.MINUTES.toMillis(5));

    private final HbaseOperations2 hbaseTemplate;

    private final ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatData> descriptor;

    private final AcceptedTimeService acceptedTimeService;


    public HbaseAddressStatDataDao(HbaseOperations2 hbaseTemplate, ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatData> descriptor, AcceptedTimeService acceptedTimeService) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
    }

    @Override
    public void insert(LocalAddressesVo localAddresses) {
        String agentId = localAddresses.getAgentId();

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        long timeSlot = this.timeSlot.getTimeSlot(acceptedTime);

        byte[] rowKey = createRowKey(agentId, timeSlot);

        List<LocalIpVo> localIpAddressList = localAddresses.getLocalIpAddressList();
        byte[] value = createValue(localIpAddressList);

        final Put put = new Put(rowKey);
        put.addColumn(descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_STRING, value);

        hbaseTemplate.put(descriptor.getTableName(), put);
    }


    private static byte[] createRowKey(String agentId, long timestamp) {
        final byte[] agentIdBytes = BytesUtils.toBytes(agentId);

        final Buffer buffer = new AutomaticBuffer(2 + agentIdBytes.length + 8);

        buffer.putShort((short) agentIdBytes.length);
        buffer.putBytes(agentIdBytes);

        long reverseTimeMillis = TimeUtils.reverseTimeMillis(timestamp);
        buffer.putLong(reverseTimeMillis);
        return buffer.getBuffer();
    }

    private static byte[] createValue(List<LocalIpVo> localIpVoList) {
        final Buffer buffer = new AutomaticBuffer(5 * localIpVoList.size());

        for (LocalIpVo localIpVo : localIpVoList) {
            byte[] srcIp = localIpVo.getSrcIp();
            byte prefix = InetFamily.getFamilyByAddressSize(srcIp.length).getByteNumber();
            buffer.putByte(prefix);
            buffer.putBytes(srcIp);
        }

        return buffer.getBuffer();
    }

}
