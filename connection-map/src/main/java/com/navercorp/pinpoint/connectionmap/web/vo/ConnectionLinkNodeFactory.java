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
public class ConnectionLinkNodeFactory {

    public static ConnectionLinkNode createSrc(ConnectionStatVo connectionStatVo, boolean includeAgentInfo) {
        final ConnectionAddress srcConnectionAddress = ConnectionAddressFactory.createSrc(connectionStatVo);
        return create(srcConnectionAddress, connectionStatVo, includeAgentInfo);
    }

    public static ConnectionLinkNode createDst(ConnectionStatVo connectionStatVo, boolean includeAgentInfo) {
        final ConnectionAddress dstConnectionAddress = ConnectionAddressFactory.createDst(connectionStatVo);
        return create(dstConnectionAddress, connectionStatVo, includeAgentInfo);
    }

    public static ConnectionLinkNode create(ConnectionAddress connectionAddress) {
        return create(connectionAddress, null, false);
    }

    public static ConnectionLinkNode create(ConnectionAddress connectionAddress, ConnectionStatVo connectionStatVo, boolean includeAgentInfo) {
        if (includeAgentInfo) {
            return new ConnectionLinkNode(connectionAddress, new ConnectionNode(connectionStatVo.getAgentId(), connectionStatVo.getPid()));
        } else {
            return new ConnectionLinkNode(connectionAddress);
        }
    }

}
