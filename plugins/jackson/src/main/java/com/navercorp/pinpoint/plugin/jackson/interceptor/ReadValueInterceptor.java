/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jackson.interceptor;

import java.io.File;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.jackson.JacksonConstants;

/**
 * @see JacksonPlugin#intercept_ObjectMapper(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext)
 * @author Sungkook Kim
 */
public class ReadValueInterceptor implements SimpleAroundInterceptor, JacksonConstants {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public ReadValueInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder frame = trace.traceBlockBegin();
        frame.markBeforeTime();
        frame.recordServiceType(SERVICE_TYPE);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            
            Object arg = args[0];
            
            if (arg != null) {
                if (arg instanceof String) {
                    recorder.recordAttribute(ANNOTATION_KEY_LENGTH_VALUE, ((String) arg).length());
                } else if (arg instanceof byte[]) {
                    recorder.recordAttribute(ANNOTATION_KEY_LENGTH_VALUE, ((byte[]) arg).length);
                } else if (arg instanceof File) {
                    recorder.recordAttribute(ANNOTATION_KEY_LENGTH_VALUE, ((File) arg).length());
                }
            }

            recorder.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}