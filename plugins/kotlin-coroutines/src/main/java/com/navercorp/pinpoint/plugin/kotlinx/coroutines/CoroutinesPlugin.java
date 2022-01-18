/*
 * Copyright 2022 NAVER Corp.
 *
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.VarArgs;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.DispatchInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.ResumeWithInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.ScheduleResumeInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class CoroutinesPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final CoroutinesConfig config = new CoroutinesConfig(context.getConfig());

        final String simpleClazzName = this.getClass().getSimpleName();

        if (!config.isTraceCoroutines()) {
            logger.info("{} disabled", simpleClazzName);
            return;
        }

        logger.info("{} config:{}", simpleClazzName, config);

        /**
         * 1. Starts coroutine task
         * 2. Creates DispatchedContinuation
         * 3. Dispatches DispatchedContinuation
         *  L addCoroutineDispatcherTransformer()
         * 4. Creates CancellableContinuation based on DispatchedContinuation
         *  L propagateAsyncContextTransformer
         * 5. Dispatches CancellableContinuation
         * 6. Executes task(actual implementation)
         *  L addExecuteTaskTransformer
         */
        addCoroutineDispatcherTransformer();
        addResumeWithTransformer();
        addCombindContextTransformer();
    }

    private void addCoroutineDispatcherTransformer() {
        Matcher dispatcherMatcher = Matchers.newPackageBasedMatcher("kotlinx.coroutines",
                new SuperClassInternalNameMatcherOperand("kotlinx.coroutines.CoroutineDispatcher", true));
        transformTemplate.transform(dispatcherMatcher, CoroutineDispatcherTransform.class);
    }

    private void addCombindContextTransformer() {
        transformTemplate.transform("kotlin.coroutines.CombinedContext", CombinedContextTransform.class);
    }

    private void addResumeWithTransformer() {
        Matcher matcher = Matchers.newClassBasedMatcher("kotlinx.coroutines.Continuation");
        transformTemplate.transform(matcher, ContinuationTransform.class);

        transformTemplate.transform("kotlin.coroutines.jvm.internal.BaseContinuationImpl", ContinuationTransform.class);
    }

    public static class CoroutineDispatcherTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            List<InstrumentMethod> dispatch = target.getDeclaredMethods(MethodFilters.name("dispatch"));
            for (InstrumentMethod instrumentMethod : dispatch) {
                instrumentMethod.addInterceptor(DispatchInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            List<InstrumentMethod> scheduleResumeAfterDelay = target.getDeclaredMethods(MethodFilters.name("scheduleResumeAfterDelay"));
            for (InstrumentMethod instrumentMethod : scheduleResumeAfterDelay) {
                instrumentMethod.addInterceptor(ScheduleResumeInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            return target.toBytecode();
        }
    }

    public static class ContinuationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            List<InstrumentMethod> resumeWith = target.getDeclaredMethods(MethodFilters.name("resumeWith"));
            for (InstrumentMethod instrumentMethod : resumeWith) {
                instrumentMethod.addInterceptor(ResumeWithInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            return target.toBytecode();
        }

    }

    public static class CombinedContextTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }

    }

}
