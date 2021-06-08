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

package com.navercorp.pinpoint.connectionmap.web.service;

import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.web.map.ConnectionLinkDataMap;

import java.time.temporal.ValueRange;
import java.util.List;

/**
 * @author Taejin Koo
 */
public interface ConnectionDataMapService {

    List<ConnectionStatVo> selectIncomingConnectionDataMap(byte[] dstAddressIp, int dstPort, ValueRange range);

    List<ConnectionStatVo> selectOutgoingConnectionDataMap(byte[] srcAddressIp, int srcPort, ValueRange range);

    ConnectionLinkDataMap selectIncomingConnectionDataMap(String agentId, byte[] dstAddressIp, int pid, ValueRange range);

    ConnectionLinkDataMap selectOutgoingConnectionDataMap(String agentId, byte[] srcAddressIp, int pid, ValueRange range);

}
