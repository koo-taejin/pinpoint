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

import com.navercorp.pinpoint.connectionmap.common.vo.LocalAddressesVo;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;
import com.navercorp.pinpoint.connectionmap.web.dao.AddressStatDataDao;

import org.springframework.stereotype.Service;

import java.time.temporal.ValueRange;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Service
public class AddressStatServiceImpl implements AddressStatService {

    private final AddressStatDataDao addressStatDataDao;

    public AddressStatServiceImpl(AddressStatDataDao addressStatDataDao) {
        this.addressStatDataDao = Objects.requireNonNull(addressStatDataDao, "addressStatDataDao");
    }

    @Override
    public Set<LocalIpVo> selectLocalAddresses(String agentId, ValueRange range) {
        List<LocalAddressesVo> localAddresses = addressStatDataDao.getLocalAddresses(agentId, range);

        Set<LocalIpVo> result = new HashSet<>();
        for (LocalAddressesVo localAddress : localAddresses) {
            result.addAll(localAddress.getLocalIpAddressList());
        }

        return result;
    }

}
