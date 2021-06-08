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

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Taejin Koo
 */
public class ServerRequestFactoryFunction implements Function<Message<? extends GeneratedMessageV3>, ServerRequest<GeneratedMessageV3>> {

    public final ServerRequestFactory serverRequestFactory;

    public ServerRequestFactoryFunction(ServerRequestFactory serverRequestFactory) {
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
    }

    @Override
    public ServerRequest<GeneratedMessageV3> apply(Message<? extends GeneratedMessageV3> message) {
        ServerRequest<? extends GeneratedMessageV3> serverRequest = null;
        try {
            serverRequest = serverRequestFactory.newServerRequest(message);
            return (ServerRequest<GeneratedMessageV3>) serverRequest;
        } catch (StatusException e) {
            throw new StatusRuntimeException(e.getStatus());
        }
    }
}
