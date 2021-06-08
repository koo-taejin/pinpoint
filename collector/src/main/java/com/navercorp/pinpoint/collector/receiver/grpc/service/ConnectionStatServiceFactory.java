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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.util.ServerRequestFactoryFunction;
import com.navercorp.pinpoint.connectionmap.collector.handler.grpc.ConnectionStatHandler;
import com.navercorp.pinpoint.connectionmap.collector.receiver.grpc.service.GrpcConnectionStatService;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.server.AgentHeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ConnectionStatServiceFactory extends AbstractServerServiceFactory {

    private ConnectionStatHandler connectionStatHandler;

    public void setConnectionStatHandler(ConnectionStatHandler connectionStatHandler) {
        this.connectionStatHandler = connectionStatHandler;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
        Objects.requireNonNull(connectionStatHandler, "connectionStatHandler");
    }

    @Override
    protected ServerServiceDefinition newServerServiceDefinition() {
        final ServerRequestFactoryFunction serverRequestFactoryFunction = new ServerRequestFactoryFunction(serverRequestFactory);

        BindableService bindableService = new GrpcConnectionStatService(serverRequestFactoryFunction, connectionStatHandler);
        return bindableService.bindService();
    }

    @Override
    public ServerServiceDefinition getObject() throws Exception {
        HeaderReader<Header> headerReader = new AgentHeaderReader("networkConnection");
        ServerInterceptor interceptor = new HeaderPropagationInterceptor(headerReader);

        final ServerServiceDefinition serverServiceDefinition = newServerServiceDefinition();
        return ServerInterceptors.intercept(serverServiceDefinition, interceptor);
    }

}
