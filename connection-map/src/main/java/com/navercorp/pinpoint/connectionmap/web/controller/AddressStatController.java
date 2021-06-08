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

package com.navercorp.pinpoint.connectionmap.web.controller;

import com.navercorp.pinpoint.connectionmap.common.vo.LocalIpVo;
import com.navercorp.pinpoint.connectionmap.web.service.AddressStatService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.temporal.ValueRange;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@Controller
public class AddressStatController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AddressStatService addressStatService;

    @RequestMapping(value = "/getLocalAddresses", method = RequestMethod.GET, params = {"agentId", "from", "to"})
    @ResponseBody
    public Set<LocalIpVo> getAgentList(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        ValueRange range = ValueRange.of(from, to);

        logger.debug("getLocalAddresses() started.");
        Set<LocalIpVo> localIpVoSet = this.addressStatService.selectLocalAddresses(agentId, range);
        logger.debug("getLocalAddresses() completed.");

        return localIpVoSet;
    }

}
