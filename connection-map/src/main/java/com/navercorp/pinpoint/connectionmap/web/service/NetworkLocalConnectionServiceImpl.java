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

import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;

import org.springframework.stereotype.Service;

import java.time.temporal.ValueRange;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Service
public class NetworkLocalConnectionServiceImpl implements NetworkLocalConnectionService {

    private final AddressStatService addressStatService;

    public NetworkLocalConnectionServiceImpl(AddressStatService addressStatService) {
        this.addressStatService = Objects.requireNonNull(addressStatService, "addressStatService");
    }

    @Override
    public Set<LocalIpVo> selectLocalAddressConnectionInfo(String agentId, ValueRange range) {
        Set<LocalIpVo> localAddressesVoList = addressStatService.selectLocalAddresses(agentId, range);
        return localAddressesVoList;
    }

}
