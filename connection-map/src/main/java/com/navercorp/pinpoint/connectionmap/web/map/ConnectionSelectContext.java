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

import com.navercorp.pinpoint.connectionmap.web.util.SearchDepth;
import com.navercorp.pinpoint.connectionmap.web.vo.ConnectionNode;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ConnectionSelectContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ValueRange range;
    private final SearchDepth callerDepth;
    private final SearchDepth calleeDepth;
    private final ConnectionVisitChecker visitChecker;

    private final Set<ConnectionNode> nextConnectionNode = Sets.newConcurrentHashSet();

    public ConnectionSelectContext(ValueRange range, SearchDepth callerDepth, SearchDepth calleeDepth, ConnectionVisitChecker visitChecker) {
        this.range = Objects.requireNonNull(range, "range");
        this.callerDepth = Objects.requireNonNull(callerDepth, "callerDepth");
        this.calleeDepth = Objects.requireNonNull(calleeDepth, "calleeDepth");
        this.visitChecker = Objects.requireNonNull(visitChecker, "visitChecker");
    }

    public ValueRange getRange() {
        return range;
    }

    public int getIncomingDepth() {
        return callerDepth.getDepth();
    }

    public int getOutgoingDepth() {
        return calleeDepth.getDepth();
    }

    public boolean isIncomingDepthOverflow() {
        return callerDepth.isDepthOverflow();
    }

    public boolean isOutgoingDepthOverflow() {
        return calleeDepth.isDepthOverflow();
    }

    public boolean visitIncoming(ConnectionNode connectionNode) {
        return visitChecker.visitIncomingNode(connectionNode);
    }

    public boolean visitOutgoing(ConnectionNode connectionNode) {
        return visitChecker.visitOutgoingNode(connectionNode);
    }

    public boolean checkVisitedIncoming(ConnectionNode connectionNode) {
        return visitChecker.isVisitedIncomingNode(connectionNode);
    }

    public boolean checkVisitedOutgoing(ConnectionNode connectionNode) {
        return visitChecker.isVisitedOutgoingNode(connectionNode);
    }

    public void addNextConnectionNode(ConnectionNode connectionNode) {
        final boolean add = this.nextConnectionNode.add(connectionNode);
        if (!add) {
            logger.debug("already added. nextConnectionNode:{}", connectionNode);
        }
    }

    public List<ConnectionNode> getNextConnectionNodeList() {
        List<ConnectionNode> nextConnectionNodeList= new ArrayList<>(this.nextConnectionNode);
        return nextConnectionNodeList;
    }

    public ConnectionSelectContext advance() {
        SearchDepth nextCallerDepth = callerDepth.nextDepth();
        SearchDepth nextCalleeDepth = calleeDepth.nextDepth();
        ConnectionSelectContext nextContext = new ConnectionSelectContext(range, nextCallerDepth, nextCalleeDepth, visitChecker);
        return nextContext;
    }

}
