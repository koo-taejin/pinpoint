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

package com.navercorp.pinpoint.connectionmap.web.map;

import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.web.util.TimeWindow;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ConnectionLinkValue {

    private final Map<Long, TransferDataStat> transferDataStatMap = new HashMap<>();

    private final TimeWindow timeWindow;

    public ConnectionLinkValue(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }

    public void addLinkData(ConnectionStatVo connectionStatVo) {
        TransferDataStat dataSet = getDataSet(connectionStatVo.getTimestamp());

        long sentBytes = connectionStatVo.getSentBytes();
        long recvBytes = connectionStatVo.getRecvBytes();
        int lastRetransmits = connectionStatVo.getLastRetransmits();

        dataSet.addSentBytes(sentBytes);
        dataSet.addReceiveBytes(recvBytes);
        dataSet.addRetransmits(lastRetransmits);

        long monotonicSentBytes = connectionStatVo.getMonotonicSentBytes();
        long monotonicRecvBytes = connectionStatVo.getMonotonicRecvBytes();
        int monotonicRetransmits = connectionStatVo.getMonotonicRetransmits();

        dataSet.updateMaxSentBytes(monotonicSentBytes);
        dataSet.updateMaxReceiveBytes(monotonicRecvBytes);
        dataSet.updateMaxRetransmits(monotonicRetransmits);
    }

    public void merge(ConnectionLinkValue connectionLinkValue) {
        Map<Long, TransferDataStat> transferDataStatMap = connectionLinkValue.getTransferDataStatMap();
        for (Map.Entry<Long, TransferDataStat> longTransferDataStatEntry : transferDataStatMap.entrySet()) {
            Long key = longTransferDataStatEntry.getKey();
            TransferDataStat value = longTransferDataStatEntry.getValue();

            TransferDataStat dataSet = getDataSet(key);

            dataSet.addSentBytes(value.getSentBytes());
            dataSet.addReceiveBytes(value.getReceiveBytes());
            dataSet.addRetransmits(value.getRetransmits());

            dataSet.updateMaxSentBytes(value.getMonotonicSentBytes());
            dataSet.updateMaxReceiveBytes(value.getMonotonicReceiveBytes());
            dataSet.updateMaxRetransmits(value.getMonotonicRetransmits());
        }
    }

    private TransferDataStat getDataSet(long timeStamp) {
        long key = timeWindow != null ? timeWindow.refineTimestamp(timeStamp) : timeStamp;
        TransferDataStat transferDataStat = transferDataStatMap.get(key);
        if (transferDataStat == null) {
            transferDataStat = new TransferDataStat();
            transferDataStatMap.put(key, transferDataStat);
        }
        return transferDataStat;
    }

    public Map<Long, TransferDataStat> getTransferDataStatMap() {
        return transferDataStatMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionLinkData{");
        sb.append("transferDataStatMap=").append(transferDataStatMap);
        sb.append(", timeWindow=").append(timeWindow);
        sb.append('}');
        return sb.toString();
    }

}
