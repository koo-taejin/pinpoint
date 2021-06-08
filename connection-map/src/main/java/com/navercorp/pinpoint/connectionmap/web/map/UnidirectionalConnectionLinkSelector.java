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

import com.navercorp.pinpoint.connectionmap.web.service.NetworkMapLocalService;
import com.navercorp.pinpoint.connectionmap.web.util.SearchDepth;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class UnidirectionalConnectionLinkSelector implements ConnectionLinkSelector {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConnectionMapCreator connectionMapCreator;
    private final NetworkMapLocalService networkMapLocalService;

    public UnidirectionalConnectionLinkSelector(NetworkMapLocalService networkMapLocalService, ConnectionMapCreator connectionMapCreator) {
        this.networkMapLocalService = Objects.requireNonNull(networkMapLocalService, "networkMapLocalService");
        this.connectionMapCreator = Objects.requireNonNull(connectionMapCreator, "connectionMapCreator");
    }

    @Override
    public ConnectionLinkDataMap select(String agentId, ValueRange range, int callerSearchDepth, int calleeSearchDepth) {
        logger.debug("Creating connectionLink data map for {}", agentId);
        final SearchDepth callerDepth = new SearchDepth(callerSearchDepth);
        final SearchDepth calleeDepth = new SearchDepth(calleeSearchDepth);

        ConnectionVisitChecker connectionVisitChecker = new ConnectionVisitChecker();

        ConnectionLinkDataMap result = networkMapLocalService.selectLocalMap(agentId, range);

        ConnectionNode connectionNode = new ConnectionNode(agentId, -1);

        List<ConnectionNode> outgoingNodeList = Arrays.asList(connectionNode);
        ConnectionSelectContext outgoingContext = new ConnectionSelectContext(range, callerDepth, new SearchDepth(0), connectionVisitChecker);
        List<ConnectionNode> incomingNodeList = Arrays.asList(connectionNode);
        ConnectionSelectContext incomingContext = new ConnectionSelectContext(range, new SearchDepth(0), calleeDepth, connectionVisitChecker);

        while (!outgoingNodeList.isEmpty() || !incomingNodeList.isEmpty()) {
            logger.info("outbound depth search start. callerDepth:{}, calleeDepth:{}, nodes:{}", outgoingContext.getOutgoingDepth(), outgoingContext.getIncomingDepth(), outgoingNodeList);
            ConnectionLinkDataMap outgoingMap = connectionMapCreator.create(outgoingNodeList, outgoingContext);
            result.merge(outgoingMap);

            logger.info("outbound depth search end.");

            logger.info("outbound depth search start. callerDepth:{}, calleeDepth:{}, nodes:{}", incomingContext.getOutgoingDepth(), incomingContext.getIncomingDepth(), incomingNodeList);
            ConnectionLinkDataMap incomingMap = connectionMapCreator.create(incomingNodeList, incomingContext);
            result.merge(incomingMap);
            logger.info("inbound depth search end.");

            outgoingNodeList = outgoingContext.getNextConnectionNodeList();
            incomingNodeList = incomingContext.getNextConnectionNodeList();

            outgoingContext = outgoingContext.advance();
            incomingContext = incomingContext.advance();
        }
        return result;
    }

}
