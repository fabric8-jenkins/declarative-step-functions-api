/**
 * Copyright (C) Original Authors 2017
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jenkins.functions.loader.support;

import io.jenkins.functions.loader.StepMetadata;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Implements a step function using a {@link Function} object which takes a context argument containing the arguments
 * or a Map
 */
public class ContextStepFunction extends StepFunctionSupport {
    private final Method method;
    private final Class<?> contextType;

    public ContextStepFunction(String name, Class<?> clazz, StepMetadata metadata, Method applyMethod) {
        super(name, clazz, metadata);
        this.method = applyMethod;
        this.contextType = applyMethod.getParameterTypes()[0];
    }

    protected Object invokeOnInstance(Map<String, Object> arguments, Object object) {
        Object context = null;
        if (Map.class.isAssignableFrom(contextType)) {
            context = arguments;
        } else {
            // lets try instantiate the context object and inject the parameters
            try {
                context = contextType.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not instantiate class " + contextType.getName() + " due to: " + e, e);
            }
            if (arguments != null) {
                Set<Map.Entry<String, Object>> entries = arguments.entrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    String name = entry.getKey();
                    Object value = entry.getValue();
                    try {
                        PropertyUtils.setProperty(context, name, value);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Could not set property " + name + " on bean " + object + " to value " + value + " due to: " + e, e);
                    }
                }
            }
        }
        Object[] args = {context};
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        }
    }
}
