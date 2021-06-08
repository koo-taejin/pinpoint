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

import com.navercorp.pinpoint.connectionmap.web.view.TransferDataStatSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = TransferDataStatSerializer.class)
public class TransferDataStat {

    private long monotonicSentBytes;
    private long sentBytes;

    private long monotonicReceiveBytes;
    private long receiveBytes;

    private long monotonicRetransmits;
    private long retransmits;


    public TransferDataStat() {
    }

    public void addSentBytes(long sentBytes) {
        this.sentBytes += sentBytes;
    }

    public void addReceiveBytes(long receiveBytes) {
        this.receiveBytes += receiveBytes;
    }

    public void addRetransmits(long retransmits) {
        this.retransmits += retransmits;
    }

    public void updateMaxSentBytes(long monotonicSentBytes) {
        this.monotonicSentBytes = Math.max(this.monotonicSentBytes, monotonicSentBytes);
    }

    public void updateMaxReceiveBytes(long monotonicReceiveBytes) {
        this.monotonicReceiveBytes = Math.max(this.monotonicReceiveBytes, monotonicReceiveBytes);
    }

    public void updateMaxRetransmits(long monotonicRetransmits) {
        this.monotonicRetransmits = Math.max(this.monotonicRetransmits, monotonicRetransmits);
    }

    public long getMonotonicSentBytes() {
        return monotonicSentBytes;
    }

    public long getSentBytes() {
        return sentBytes;
    }

    public long getMonotonicReceiveBytes() {
        return monotonicReceiveBytes;
    }

    public long getReceiveBytes() {
        return receiveBytes;
    }

    public long getMonotonicRetransmits() {
        return monotonicRetransmits;
    }

    public long getRetransmits() {
        return retransmits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransferDataStat{");
        sb.append("monotonicSentBytes=").append(monotonicSentBytes);
        sb.append(", sentBytes=").append(sentBytes);
        sb.append(", monotonicReceiveBytes=").append(monotonicReceiveBytes);
        sb.append(", receiveBytes=").append(receiveBytes);
        sb.append(", monotonicRetransmits=").append(monotonicRetransmits);
        sb.append(", retransmits=").append(retransmits);
        sb.append('}');
        return sb.toString();
    }
}
