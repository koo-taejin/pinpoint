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

import com.navercorp.pinpoint.connectionmap.web.map.ConnectionLinkDataMap;
import com.navercorp.pinpoint.connectionmap.web.map.ConnectionLinkSelector;
import com.navercorp.pinpoint.connectionmap.web.map.ConnectionMap;
import com.navercorp.pinpoint.connectionmap.web.map.NetworkConnectionMapBuilder;

import org.springframework.stereotype.Service;

import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class ConnectionMapServiceImpl implements ConnectionMapService {

    private final ConnectionLinkSelector connectionLinkSelector;
    private final NetworkConnectionMapBuilder networkConnectionMapBuilder = new NetworkConnectionMapBuilder();

    public ConnectionMapServiceImpl(ConnectionLinkSelector connectionLinkSelector) {
        this.connectionLinkSelector = Objects.requireNonNull(connectionLinkSelector, "connectionLinkSelector");
    }

    @Override
    public ConnectionMap selectMap(String agentId, ValueRange range, int callerSearchDepth, int calleeSearchDepth) {
        ConnectionLinkDataMap connectionLinkDataMap = connectionLinkSelector.select(agentId, range, callerSearchDepth, calleeSearchDepth);
        return networkConnectionMapBuilder.build(connectionLinkDataMap);
    }

}
