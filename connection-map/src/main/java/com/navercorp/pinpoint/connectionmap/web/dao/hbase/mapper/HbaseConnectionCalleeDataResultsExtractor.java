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

package com.navercorp.pinpoint.connectionmap.web.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.connectionmap.common.vo.ConnectionStatVo;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class HbaseConnectionCalleeDataResultsExtractor implements ResultsExtractor<List<ConnectionStatVo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseConnectionCalleeDataMapper connectionCalleeDataMapper = new HbaseConnectionCalleeDataMapper();

    @Override
    public List<ConnectionStatVo> extractData(ResultScanner results) throws Exception {
        List<ConnectionStatVo> connectionStatVoList = new ArrayList<>();

        int index = 0;
        for (Result result : results) {
            try {
                index++;
                ConnectionStatVo connectionStatVo = connectionCalleeDataMapper.mapRow(result, index);
                connectionStatVoList.add(connectionStatVo);
            } catch (Exception e) {
                logger.warn("Failed to convert connectionStatVo. message:{}", e.getMessage(), e);
            }
        }

        return connectionStatVoList;
    }

}
