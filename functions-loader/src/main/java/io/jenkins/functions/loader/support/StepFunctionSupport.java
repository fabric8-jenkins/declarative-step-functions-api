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
package io.jenkins.functions.loader.support;

import io.jenkins.functions.Logger;
import io.jenkins.functions.loader.FunctionContext;
import io.jenkins.functions.loader.StepFunction;
import io.jenkins.functions.loader.StepMetadata;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.File;
import java.util.Map;

/**
 */
public abstract class StepFunctionSupport implements StepFunction {
    private final String name;
    private final Class<?> clazz;
    private final StepMetadata metadata;

    public StepFunctionSupport(String name, Class<?> clazz, StepMetadata metadata) {
        this.name = name;
        this.clazz = clazz;
        this.metadata = metadata;
    }

    @Override
    public Object invoke(Map<String, Object> arguments, FunctionContext context) {
        Object object;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate class " + clazz.getName() + " due to: " + e, e);
        }
        injectContext(object, context);
        return invokeOnInstance(arguments, context, object);
    }

    protected void injectContext(Object object, FunctionContext context) {
        Logger logger = context.getLogger();
        if (logger != null) {
            setBeanPropertty(object, "logger", logger);
        }
        File currentDir = context.getCurrentDir();
        if (currentDir != null) {
            setBeanPropertty(object, "currentDir", currentDir);
        }
    }

    protected void setBeanPropertty(Object object, String name, Object value) {
        try {
            PropertyUtils.setProperty(object, name, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to set property " + name + " on function object " + object + " due to: " + e, e);
        }
    }

    protected abstract Object invokeOnInstance(Map<String, Object> arguments, FunctionContext context, Object object);

    @Override
    public StepMetadata getMetadata() {
        return metadata;
    }

    public String getName() {
        return getMetadata().getName();
    }
}
