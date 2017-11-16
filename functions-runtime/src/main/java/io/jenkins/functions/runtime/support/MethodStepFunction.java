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

import io.jenkins.functions.Argument;
import io.jenkins.functions.Step;
import io.jenkins.functions.runtime.FunctionContext;
import io.jenkins.functions.runtime.StepMetadata;
import io.jenkins.functions.runtime.helpers.Strings;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Implements a step function using a regular method that is annotated with {@link Step}
 */
public class MethodStepFunction extends StepFunctionSupport {
    private final Method method;

    public MethodStepFunction(String name, Class<?> clazz, StepMetadata metadata, Method method) {
        super(name, clazz, metadata);
        this.method = method;
    }

    protected Object invokeOnInstance(Map<String, Object> arguments, FunctionContext context, Object object) {
        Parameter[] parameters = method.getParameters();
        if (parameters == null) {
            parameters = new Parameter[0];
        }
        int idx = 0;
        Object[] args = new Object[parameters.length];
        for (Parameter parameter : parameters) {
            Class<?> type = parameter.getType();
            String name = null;
            Argument argument = parameter.getAnnotation(Argument.class);
            if (argument != null) {
                name = argument.name();
            }
            if (Strings.isNullOrEmpty(name)) {
                name = parameter.getName();
            }
            Object value = arguments.get(name);
            if (value == null) {
                // TODO handle default values!!!
            }
            if (value != null) {
                value = ConvertUtils.convert(value, type);
            }
            args[idx++] = value;
        }
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        }
    }
}
