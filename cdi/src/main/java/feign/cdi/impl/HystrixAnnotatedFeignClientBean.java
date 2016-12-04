/*
 * Copyright 2013 Netflix, Inc.
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

package feign.cdi.impl;

import feign.Feign;
import feign.cdi.api.EnableHystrix;
import feign.hystrix.HystrixFeign;

public class HystrixAnnotatedFeignClientBean extends AnnotatedFeignClientBean{

    private final EnableHystrix enableHystrix;

    public HystrixAnnotatedFeignClientBean(Class<?> interfaceClass) {
        super(interfaceClass);
        this.enableHystrix = getInterfaceClass().getAnnotation(EnableHystrix.class);
    }

    @Override
    protected Feign.Builder createBuilder() {
        return HystrixFeign.builder();
    }

    @Override
    protected Feign.Builder mergeBuilders(Feign.Builder builder) {
        Feign.Builder merged = super.mergeBuilders(builder);
        if(merged instanceof HystrixFeign.Builder) {
            HystrixFeign.Builder hystrixBuilder = (HystrixFeign.Builder) merged;
            hystrixBuilder.setterFactory(getBean(enableHystrix.setterFactory()));
        }
        return merged;
    }

    @Override
    protected Object doTarget(Feign.Builder builder, String url) {
        if(builder instanceof HystrixFeign.Builder) {
            HystrixFeign.Builder hystrixBuilder = (HystrixFeign.Builder) builder;
            return hystrixBuilder.target(getInterfaceClass(), url, getBean(enableHystrix.fallbackFactory()));
        }
        else {
            return super.doTarget(builder, url);
        }
    }

    @Override
    protected boolean useInvocationHandlerFactory() {
        return false;
    }
}
