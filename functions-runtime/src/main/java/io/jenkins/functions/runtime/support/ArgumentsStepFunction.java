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

import io.jenkins.functions.runtime.FunctionContext;
import io.jenkins.functions.runtime.StepMetadata;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Implements a step function using a {@link Function} object which takes an Arguments object containing the arguments
 * or a Map
 */
public class ArgumentsStepFunction extends StepFunctionSupport {
    private final Method method;
    private final Class<?> contextType;

    public ArgumentsStepFunction(String name, Class<?> clazz, StepMetadata metadata, Method applyMethod) {
        super(name, clazz, metadata);
        this.method = applyMethod;
        this.contextType = applyMethod.getParameterTypes()[0];
    }

    @Override
    public String toString() {
        return "ArgumentsStepFunction{" + getName() + "}";
    }

    protected Object invokeOnInstance(Map<String, Object> arguments, FunctionContext functionContext, Object object) {
        Object argumentObject = null;
        if (Map.class.isAssignableFrom(contextType)) {
            argumentObject = arguments;
        } else {
            // lets try instantiate the argumentObject object and inject the parameters
            try {
                argumentObject = contextType.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not instantiate class " + contextType.getName() + " due to: " + e, e);
            }
            if (arguments != null) {
                Set<Map.Entry<String, Object>> entries = arguments.entrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    String name = entry.getKey();
                    Object value = entry.getValue();
                    try {
                        PropertyUtils.setProperty(argumentObject, name, value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Could not set property " + name + " on bean " + argumentObject + " to value " + value + " due to: " + e, e);
                    }
                }
            }
        }
        Object[] args = {argumentObject};
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        }
    }
}
