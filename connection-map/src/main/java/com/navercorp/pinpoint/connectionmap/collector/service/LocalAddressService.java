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

import com.navercorp.pinpoint.connectionmap.collector.dao.AddressStatDataDao;
import com.navercorp.pinpoint.connectionmap.common.vo.LocalAddressesVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class LocalAddressService {

    private final AddressStatDataDao addressStatDataDao;

    @Autowired
    public LocalAddressService(AddressStatDataDao addressStatDataDao) {
        this.addressStatDataDao = Objects.requireNonNull(addressStatDataDao, "addressStatDataDao");
    }

    public void insert(LocalAddressesVo localAddresses) {
        addressStatDataDao.insert(localAddresses);
    }

}
