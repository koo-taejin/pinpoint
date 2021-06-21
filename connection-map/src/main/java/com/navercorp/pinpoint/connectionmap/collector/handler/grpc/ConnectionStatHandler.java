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

package com.navercorp.pinpoint.connectionmap.collector.handler.grpc;

import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.connectionmap.collector.service.ConnectionStatService;
import com.navercorp.pinpoint.connectionmap.collector.service.LocalAddressService;
import com.navercorp.pinpoint.connectionmap.collector.vo.converter.ConnectionStatConverter;
import com.navercorp.pinpoint.connectionmap.common.proto.PConnectionStats;
import com.navercorp.pinpoint.connectionmap.common.proto.PConnectionStatsMessage;
import com.navercorp.pinpoint.connectionmap.common.proto.PLocalAddressesMessage;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalAddressesVo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.io.request.ServerRequest;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class ConnectionStatHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ConnectionStatConverter converter = new ConnectionStatConverter();

    private  ConnectionStatService connectionStatService;

    private  LocalAddressService localAddressService;

    private  AcceptedTimeService acceptedTimeService;


    public ConnectionStatHandler(ConnectionStatService connectionStatService, LocalAddressService localAddressService, AcceptedTimeService acceptedTimeService) {
        this.connectionStatService = Objects.requireNonNull(connectionStatService, "connectionStatService");
        this.localAddressService = Objects.requireNonNull(localAddressService, "localAddressService");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
    }

    public void handle(ServerRequest<GeneratedMessageV3> serverRequest) {
        logger.info("handleSimple:{}", serverRequest);

        final GeneratedMessageV3 message = serverRequest.getData();

        final Context current = Context.current();
        final Header header = ServerContext.getAgentInfo(current);

        acceptedTimeService.accept();
//        PConnectionStatsMessage

        if (message instanceof PConnectionStatsMessage) {
            PConnectionStatsMessage pConnectionStatsMessage = (PConnectionStatsMessage) message;



            ((PConnectionStatsMessage) message).getConnectionStatsCollectionsMap();

        }

        if (message instanceof PConnectionStatsMessage) {
            List<PConnectionStats> connectionStatsList = ((PConnectionStatsMessage) message).getConnectionStatsList();
            for (PConnectionStats pConnectionStats : connectionStatsList) {
                ConnectionStatVo connectionStatVo = converter.convert(pConnectionStats, header);
                connectionStatService.insert(connectionStatVo);
            }
        } else if (message instanceof PLocalAddressesMessage) {
            LocalAddressesVo localAddressesVo = converter.convert((PLocalAddressesMessage) message, header);
            localAddressService.insert(localAddressesVo);
        }
    }

}
