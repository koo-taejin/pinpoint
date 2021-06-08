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

package com.navercorp.pinpoint.connectionmap.web.vo;


import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;

/**
 * @author Taejin Koo
 */
public class ConnectionAddressFactory {

    public static ConnectionAddress createSrc(ConnectionStatVo connectionStatVo) {
        final byte[] srcIp = connectionStatVo.getSrcIp();
        final int srcPort = connectionStatVo.getSrcPort();

        return new ConnectionAddress(srcIp, srcPort);
    }

    public static ConnectionAddress createDst(ConnectionStatVo connectionStatVo) {
        final byte[] dstIp = connectionStatVo.getDstIp();
        final int dstPort = connectionStatVo.getDstPort();

        return new ConnectionAddress(dstIp, dstPort);
    }

}
