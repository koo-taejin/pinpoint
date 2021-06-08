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

import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionNode;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ConnectionVisitChecker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Set<ConnectionNode> foundIncomingNode = Sets.newConcurrentHashSet();
    private final Set<ConnectionNode> foundOutgoingNode = Sets.newConcurrentHashSet();

    public boolean visitIncomingNode(ConnectionNode connectionNode) {
        final boolean alreadyVisited = !foundIncomingNode.add(connectionNode);
        if (logger.isDebugEnabled()) {
            if (alreadyVisited) {
                logger.debug("LinkData exists. Skip finding {}. ", connectionNode);
            }
        }
        return alreadyVisited;
    }

    public boolean isVisitedIncomingNode(ConnectionNode connectionNode) {
        return foundIncomingNode.contains(connectionNode);
    }

    public boolean visitOutgoingNode(ConnectionNode connectionNode) {
        final boolean alreadyVisited = !foundOutgoingNode.add(connectionNode);
        if (logger.isDebugEnabled()) {
            if (alreadyVisited) {
                logger.debug("LinkData exists. Skip finding {}. ", connectionNode);
            }
        }
        return alreadyVisited;
    }

    public boolean isVisitedOutgoingNode(ConnectionNode connectionNode) {
        return foundOutgoingNode.contains(connectionNode);
    }

}
