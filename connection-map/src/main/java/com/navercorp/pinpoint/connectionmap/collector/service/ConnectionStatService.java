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

package com.navercorp.pinpoint.connectionmap.collector.service;

import com.navercorp.pinpoint.connectionmap.collector.dao.ConnectionStatCalleeDao;
import com.navercorp.pinpoint.connectionmap.collector.dao.ConnectionStatCallerDao;
import com.navercorp.pinpoint.connectionmap.collector.dao.ConnectionStatSelfDao;
import com.navercorp.pinpoint.connectionmap.collector.dao.hbase.HbasePidIncrementerDao;
import com.navercorp.pinpoint.connectionmap.common.proto.Direction;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class ConnectionStatService {

    private final ConnectionStatCallerDao connectionStatCallerDao;

    private final ConnectionStatCalleeDao connectionStatCalleeDao;

    private final ConnectionStatSelfDao connectionStatSelfDao;

    private final HbasePidIncrementerDao hbasePidIncrementerDao;

    public ConnectionStatService(ConnectionStatCallerDao connectionStatCallerDao, ConnectionStatCalleeDao connectionStatCalleeDao, ConnectionStatSelfDao connectionStatSelfDao, HbasePidIncrementerDao hbasePidIncrementerDao) {
        this.connectionStatCallerDao = Objects.requireNonNull(connectionStatCallerDao, "connectionStatCallerDao");
        this.connectionStatCalleeDao = Objects.requireNonNull(connectionStatCalleeDao, "connectionStatCalleeDao");
        this.connectionStatSelfDao = Objects.requireNonNull(connectionStatSelfDao, "connectionStatSelfDao");
        this.hbasePidIncrementerDao = Objects.requireNonNull(hbasePidIncrementerDao, "hbasePidIncrementerDao");
    }

    public void insert(final ConnectionStatVo connectionStat) {
        Objects.requireNonNull(connectionStat, "connectionStat");

        final Direction direction = connectionStat.getDirection();
        if (direction == Direction.INCOMING) {
            connectionStatCallerDao.insert(connectionStat);
        } else if (direction == Direction.OUTGOING) {
            connectionStatCalleeDao.insert(connectionStat);
        } else if (direction == Direction.LOCAL) {
            connectionStatSelfDao.insert(connectionStat);
        }

        hbasePidIncrementerDao.increment(connectionStat.getAgentId(), connectionStat.getPid());
    }

}
