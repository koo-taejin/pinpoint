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

package com.navercorp.pinpoint.connectionmap.common.hbase;

import com.navercorp.pinpoint.common.hbase.TableNameProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Taejin Koo
 */
@Configuration()
public class ConnectionMapTableDescriptorConfig extends ConnectionMapTableDescriptors {

    public ConnectionMapTableDescriptorConfig(TableNameProvider tableNameProvider) {
        super(tableNameProvider);
    }

    @Bean
    @Override
    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatData> getConnectionStatData() {
        return super.getConnectionStatData();
    }

    @Bean
    @Override
    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.PidStatData> getPidStatData() {
        return super.getPidStatData();
    }

    @Bean
    @Override
    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCallee> getConnectionStatCallee() {
        return super.getConnectionStatCallee();
    }

    @Bean
    @Override
    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatCaller> getConnectionStatCaller() {
        return super.getConnectionStatCaller();
    }

    @Bean
    @Override
    public ConnectionMapTableDescriptor<ConnectionMapHbaseColumnFamily.ConnectionStatSelf> getConnectionStatSelf() {
        return super.getConnectionStatSelf();
    }

}
