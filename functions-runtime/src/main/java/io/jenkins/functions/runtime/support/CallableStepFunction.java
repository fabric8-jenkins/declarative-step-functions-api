/**
 * Copyright (C) Original Authors 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.functions.runtime.support;

import io.jenkins.functions.Step;
import io.jenkins.functions.runtime.FunctionContext;
import io.jenkins.functions.runtime.StepMetadata;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Implements a step function using a {@link Callable} object annotated with {@link Step}
 */
public class CallableStepFunction extends StepFunctionSupport {
    private final Method method;

    public CallableStepFunction(String name, Class<?> clazz, StepMetadata metadata, Method callableMethod) {
        super(name, clazz, metadata);
        this.method = callableMethod;
    }

    protected Object invokeOnInstance(Map<String, Object> arguments, FunctionContext context, Object object) {
        createArgumentsObject(object, arguments);
        try {
            return method.invoke(object);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        }
    }

    @Override
    protected Object createArgumentsObject(Object object, Map<String, Object> arguments) {
        if (arguments != null) {
            Set<Map.Entry<String, Object>> entries = arguments.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                String name = entry.getKey();
                Object value = entry.getValue();
                try {
                    PropertyUtils.setProperty(object, name, value);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not set property " + name + " on bean " + object + " to value " + value + " due to: " + e, e);
                }
            }
        }
        return object;
    }
}
