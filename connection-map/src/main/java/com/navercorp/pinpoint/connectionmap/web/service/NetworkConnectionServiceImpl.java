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
import com.navercorp.pinpoint.connectionmap.web.dao.ConnectionStatCalleeDao;
import com.navercorp.pinpoint.connectionmap.web.dao.ConnectionStatCallerDao;

import org.springframework.stereotype.Service;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Service
public class NetworkConnectionServiceImpl implements NetworkConnectionService {

    private final ConnectionStatCallerDao connectionStatCallerDao;
    private final ConnectionStatCalleeDao connectionStatCalleeDao;

    public NetworkConnectionServiceImpl(ConnectionStatCallerDao connectionStatCallerDao, ConnectionStatCalleeDao connectionStatCalleeDao) {
        this.connectionStatCallerDao = connectionStatCallerDao;
        this.connectionStatCalleeDao = connectionStatCalleeDao;
    }

    @Override
    public List<ConnectionStatVo> selectIncomingConnectionDataMap(String agentId, byte[] dstAddressIp, Set<Integer> pidSet, ValueRange range) {
        Objects.requireNonNull(agentId, "agentId");
        List<ConnectionStatVo> connectionStatVos = connectionStatCallerDao.getConnectionStatVos(dstAddressIp, range);

        List<ConnectionStatVo> result = new ArrayList<>();

        for (ConnectionStatVo connectionStatVo : connectionStatVos) {
            if (agentId.equals(connectionStatVo.getAgentId())) {
                result.add(connectionStatVo);
            }
        }

        return result;
    }

    @Override
    public List<ConnectionStatVo> selectOutgoingConnectionDataMap(String agentId, byte[] srcAddressIp, Set<Integer> pidSet, ValueRange range) {
        Objects.requireNonNull(agentId, "agentId");
        List<ConnectionStatVo> connectionStatVos = connectionStatCalleeDao.getConnectionStatVos(srcAddressIp, range);

        List<ConnectionStatVo> result = new ArrayList<>();

        for (ConnectionStatVo connectionStatVo : connectionStatVos) {
            if (agentId.equals(connectionStatVo.getAgentId())) {
                result.add(connectionStatVo);
            }
        }

        return result;
    }

    @Override
    public List<ConnectionStatVo> selectIncomingConnectionDataMap(byte[] dstAddressIp, int dstPort, ValueRange range) {
        List<ConnectionStatVo> connectionStatVos = connectionStatCallerDao.getConnectionStatVos(dstAddressIp, range);

        List<ConnectionStatVo> result = new ArrayList<>();
        for (ConnectionStatVo connectionStatVo : connectionStatVos) {
            if (connectionStatVo.getDstPort() == dstPort) {
                result.add(connectionStatVo);
            }
        }
        return result;
    }

    @Override
    public List<ConnectionStatVo> selectOutgoingConnectionDataMap(byte[] srcAddressIp, int srcPort, ValueRange range) {
        List<ConnectionStatVo> connectionStatVos = connectionStatCalleeDao.getConnectionStatVos(srcAddressIp, range);

        List<ConnectionStatVo> result = new ArrayList<>();
        for (ConnectionStatVo connectionStatVo : connectionStatVos) {
            if (connectionStatVo.getSrcPort() == srcPort) {
                result.add(connectionStatVo);
            }
        }

        return result;
    }

}
