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
package io.jenkins.functions.loader;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 */
public class StepFunctionImpl implements StepFunction {
    private final String name;
    private final Class<?> clazz;
    private final Method method;
    private final StepMetadata metadata;

    public StepFunctionImpl(String name, Class<?> clazz, Method method, StepMetadata metadata) {
        this.name = name;
        this.clazz = clazz;
        this.method = method;
        this.metadata = metadata;
    }

    @Override
    public Object invoke(Map<String, Object> arguments) {
        Object object;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        }

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

        try {
            return method.invoke(object);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not invoke " + method + " due to: " + e, e);
        }
    }

    @Override
    public StepMetadata getMetadata() {
        return metadata;
    }
}
