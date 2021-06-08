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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.server.AgentHeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class HeaderServerInterceptorFactory {
    @Bean("agentInterceptorList")
    public List<ServerInterceptor> getAgentServerInterceptor() {
        return newServerInterceptors("agent");
    }

    @Bean("spanInterceptorList")
    public List<ServerInterceptor> getSpanServerInterceptor() {
        return newServerInterceptors("span");
    }

    @Bean("statInterceptorList")
    public List<ServerInterceptor> getStatServerInterceptor() {
        return newServerInterceptors("stat");
    }

    @Bean("connectionStatInterceptorList")
    public List<ServerInterceptor> getNetworkConnectionServerInterceptor() {
        return newServerInterceptors("connectionStat");
    }

    private List<ServerInterceptor> newServerInterceptors(String name) {
        HeaderReader<Header> headerReader = new AgentHeaderReader(name);
        ServerInterceptor interceptor = new HeaderPropagationInterceptor(headerReader);
        return Arrays.asList(interceptor);
    }
}
