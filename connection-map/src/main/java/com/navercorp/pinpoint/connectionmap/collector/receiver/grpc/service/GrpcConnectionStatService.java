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

package com.navercorp.pinpoint.connectionmap.collector.receiver.grpc.service;

import com.navercorp.pinpoint.connectionmap.collector.handler.grpc.ConnectionStatHandler;
import com.navercorp.pinpoint.connectionmap.common.proto.NetworkConnectionGrpc;
import com.navercorp.pinpoint.connectionmap.common.proto.PConnectionStatsMessage;
import com.navercorp.pinpoint.connectionmap.common.proto.PLocalAddressesMessage;
import com.navercorp.pinpoint.connectionmap.common.proto.PResult;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Taejin Koo
 */
public class GrpcConnectionStatService extends NetworkConnectionGrpc.NetworkConnectionImplBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Function<Message<? extends GeneratedMessageV3>, ServerRequest<GeneratedMessageV3>> serverRequestFactory;
    private final ConnectionStatHandler handler;

    public GrpcConnectionStatService(Function<Message<? extends GeneratedMessageV3>, ServerRequest<GeneratedMessageV3>> serverRequestFactory,
                                     ConnectionStatHandler handler) {
        this.serverRequestFactory = Objects.requireNonNull(serverRequestFactory, "serverRequestFactory");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public StreamObserver<PConnectionStatsMessage> sendConnectionStatsMessage(final StreamObserver<Empty> responseObserver) {
        StreamObserver<PConnectionStatsMessage> observer = new StreamObserver<PConnectionStatsMessage>() {
            @Override
            public void onNext(PConnectionStatsMessage connectionStatsMessage) {
                logger.debug("Send PConnectionStatsMessage={}", MessageFormatUtils.debugLog(connectionStatsMessage));

                final Message<PConnectionStatsMessage> message = newMessage(connectionStatsMessage);
                send(message, responseObserver);
            }

            private void send(final Message<? extends GeneratedMessageV3> message, StreamObserver<Empty> responseObserver) {
                try {
                    ServerRequest<GeneratedMessageV3> request = serverRequestFactory.apply(message);
                    handler.handle(request);
                } catch (Exception e) {
                    logger.warn("Failed to request. message={}", message, e);
                    if (e instanceof StatusException || e instanceof StatusRuntimeException) {
                        responseObserver.onError(e);
                    } else {
                        // Avoid detailed exception
                        responseObserver.onError(Status.INTERNAL.withDescription("Bad Request").asException());
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                com.navercorp.pinpoint.grpc.Header header = ServerContext.getAgentInfo();

                final StatusError statusError = StatusErrors.throwable(throwable);
                if (statusError.isSimpleError()) {
                    logger.info("Failed to connectionStats stream, {} cause={}", header, statusError.getMessage(), statusError.getThrowable());
                } else {
                    logger.warn("Failed to connectionStats stream, {} cause={}", header, statusError.getMessage(), statusError.getThrowable());
                }
            }

            @Override
            public void onCompleted() {
                com.navercorp.pinpoint.grpc.Header header = ServerContext.getAgentInfo();
                logger.info("onCompleted {}", header);

                Empty empty = Empty.newBuilder().build();
                responseObserver.onNext(empty);
                responseObserver.onCompleted();
            }
        };
        return observer;
    }


    @Override
    public void requestPLocalAddressesMessage(PLocalAddressesMessage localAddressesMessage, StreamObserver<PResult> responseObserver) {
        final Message<PLocalAddressesMessage> message = newMessage(localAddressesMessage);
        handle(message, responseObserver);
    }

    private void handle(Message<? extends GeneratedMessageV3> message, StreamObserver<PResult> responseObserver) {
        try {
            ServerRequest<GeneratedMessageV3> request = serverRequestFactory.apply(message);
            handler.handle(request);

            PResult.Builder builder = PResult.newBuilder();
            builder.setSuccess(true);

            responseObserver.onNext(builder.build());
        } catch (Exception e) {
            logger.warn("Failed to handle message. message:{}", e.getMessage(), e);
        }
    }

    private <T> Message<T> newMessage(T requestData) {
        final Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, (short) -1);
        final HeaderEntity headerEntity = new HeaderEntity(Collections.emptyMap());
        return new DefaultMessage<>(header, headerEntity, requestData);
    }

}
